package com.example.demo.pricing.infrastructure.db.repository;

import static com.example.demo.jooq.tables.Pricings.PRICINGS;

import com.example.demo.jooq.SoftDeleteCondition;
import com.example.demo.pricing.domain.model.aggregate.Pricing;
import com.example.demo.pricing.domain.model.valueobject.Price;
import com.example.demo.pricing.domain.model.valueobject.PricingLevel;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import com.example.demo.pricing.domain.repository.PricingRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Repository;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jspecify.annotations.Nullable;

/** Pricing リポジトリ実装。 */
@RequiredArgsConstructor
@Repository
public class PricingRepositoryImpl implements PricingRepository {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  /** 時計。 */
  private final Clock clock;

  @Override
  public PricingId generateId() {
    return new PricingId(UUID.randomUUID().toString());
  }

  @Override
  public void save(final Pricing pricing, final int version, final String operatorId) {
    final UUID id = UUID.fromString(pricing.getId().value());
    final UUID productId = UUID.fromString(pricing.getProductId());
    final OffsetDateTime now = OffsetDateTime.now(clock);
    final @Nullable OffsetDateTime validTo =
        pricing.getValidTo() != null ? pricing.getValidTo().atOffset(ZoneOffset.UTC) : null;

    if (version == 0) {
      dsl.insertInto(PRICINGS)
          .set(PRICINGS.ID, id)
          .set(PRICINGS.PRODUCT_ID, productId)
          .set(PRICINGS.LEVEL, pricing.getLevel().name())
          .set(PRICINGS.AREA_CODE, pricing.getAreaCode())
          .set(PRICINGS.AMOUNT, pricing.getAmount().value())
          .set(PRICINGS.VALID_FROM, pricing.getValidFrom().atOffset(ZoneOffset.UTC))
          .set(PRICINGS.VALID_TO, DSL.val(validTo, PRICINGS.VALID_TO))
          .set(PRICINGS.CREATED_AT, now)
          .set(PRICINGS.UPDATED_AT, now)
          .set(PRICINGS.CREATED_BY, operatorId)
          .set(PRICINGS.UPDATED_BY, operatorId)
          .set(PRICINGS.VERSION, 1)
          .execute();
    } else {
      dsl.update(PRICINGS)
          .set(PRICINGS.AMOUNT, pricing.getAmount().value())
          .set(PRICINGS.VALID_FROM, pricing.getValidFrom().atOffset(ZoneOffset.UTC))
          .set(PRICINGS.VALID_TO, DSL.val(validTo, PRICINGS.VALID_TO))
          .set(PRICINGS.UPDATED_AT, now)
          .set(PRICINGS.UPDATED_BY, operatorId)
          .set(PRICINGS.VERSION, version + 1)
          .where(PRICINGS.ID.eq(id))
          .and(PRICINGS.VERSION.eq(version))
          .and(SoftDeleteCondition.notDeleted(PRICINGS))
          .execute();
    }
  }

  @Override
  public Optional<Pricing> findById(final PricingId id) {
    final UUID uuid = UUID.fromString(id.value());
    return dsl.selectFrom(PRICINGS)
        .where(PRICINGS.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(PRICINGS))
        .fetchOptional()
        .map(
            r -> {
              final @Nullable Instant validTo =
                  r.getValidTo() != null ? r.getValidTo().toInstant() : null;
              return Pricing.reconstitute(
                  new PricingId(r.getId().toString()),
                  r.getProductId().toString(),
                  PricingLevel.valueOf(r.getLevel()),
                  r.getAreaCode(),
                  new Price(r.getAmount()),
                  r.getValidFrom().toInstant(),
                  validTo);
            });
  }

  @Override
  public int getVersion(final PricingId id) {
    final UUID uuid = UUID.fromString(id.value());
    return dsl.select(PRICINGS.VERSION)
        .from(PRICINGS)
        .where(PRICINGS.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(PRICINGS))
        .fetchOptional(PRICINGS.VERSION)
        .orElse(0);
  }

  @Override
  public void delete(final PricingId id, final int version, final String operatorId) {
    final UUID uuid = UUID.fromString(id.value());
    final OffsetDateTime now = OffsetDateTime.now(clock);
    dsl.update(PRICINGS)
        .set(PRICINGS.DELETED_AT, now)
        .set(PRICINGS.UPDATED_AT, now)
        .set(PRICINGS.UPDATED_BY, operatorId)
        .set(PRICINGS.VERSION, version + 1)
        .where(PRICINGS.ID.eq(uuid))
        .and(PRICINGS.VERSION.eq(version))
        .and(SoftDeleteCondition.notDeleted(PRICINGS))
        .execute();
  }

  @Override
  public boolean existsOverlapping(final Pricing pricing) {
    final UUID productId = UUID.fromString(pricing.getProductId());
    final @Nullable OffsetDateTime validTo =
        pricing.getValidTo() != null ? pricing.getValidTo().atOffset(ZoneOffset.UTC) : null;
    final OffsetDateTime validFrom = pricing.getValidFrom().atOffset(ZoneOffset.UTC);

    final Condition overlapCondition = buildOverlapCondition(validFrom, validTo);

    final UUID selfId = UUID.fromString(pricing.getId().value());
    return dsl.fetchExists(
        dsl.selectOne()
            .from(PRICINGS)
            .where(PRICINGS.PRODUCT_ID.eq(productId))
            .and(PRICINGS.LEVEL.eq(pricing.getLevel().name()))
            .and(PRICINGS.AREA_CODE.eq(pricing.getAreaCode()))
            .and(PRICINGS.ID.ne(selfId))
            .and(SoftDeleteCondition.notDeleted(PRICINGS))
            .and(overlapCondition));
  }

  @Override
  public void deleteAllByProductId(final String productId, final String operatorId) {
    final UUID uuid = UUID.fromString(productId);
    final OffsetDateTime now = OffsetDateTime.now(clock);
    dsl.update(PRICINGS)
        .set(PRICINGS.DELETED_AT, now)
        .set(PRICINGS.UPDATED_AT, now)
        .set(PRICINGS.UPDATED_BY, operatorId)
        .where(PRICINGS.PRODUCT_ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(PRICINGS))
        .execute();
  }

  private Condition buildOverlapCondition(
      final OffsetDateTime validFrom, @Nullable final OffsetDateTime validTo) {
    // (existing.validFrom < new.validTo OR new.validTo IS NULL)
    final Condition startCondition;
    if (validTo == null) {
      startCondition = DSL.trueCondition();
    } else {
      startCondition = PRICINGS.VALID_FROM.lessThan(validTo);
    }
    // (new.validFrom < existing.validTo OR existing.validTo IS NULL)
    final Condition endCondition =
        PRICINGS.VALID_TO.isNull().or(PRICINGS.VALID_TO.greaterThan(validFrom));
    return startCondition.and(endCondition);
  }
}
