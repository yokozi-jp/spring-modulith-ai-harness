package com.example.demo.pricing.infrastructure.db.query;

import static com.example.demo.jooq.tables.Pricings.PRICINGS;

import com.example.demo.jooq.SoftDeleteCondition;
import com.example.demo.pricing.application.query.dto.PricingDetailDto;
import com.example.demo.pricing.application.query.dto.PricingSummaryDto;
import com.example.demo.pricing.application.query.param.PricingListParam;
import com.example.demo.pricing.application.query.service.PricingQueryService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/** Pricing クエリサービス実装。 */
@RequiredArgsConstructor
@Component
public class PricingQueryServiceImpl implements PricingQueryService {

  /** jOOQ DSL コンテキスト。 */
  private final DSLContext dsl;

  @Override
  public Page<PricingSummaryDto> findAll(final PricingListParam param, final Pageable pageable) {
    final List<Condition> conditions = buildConditions(param);

    final Integer totalCount =
        dsl.selectCount().from(PRICINGS).where(conditions).fetchOne(0, Integer.class);
    final int total = totalCount != null ? totalCount : 0;

    final List<PricingSummaryDto> content =
        dsl.select(
                PRICINGS.ID,
                PRICINGS.PRODUCT_ID,
                PRICINGS.LEVEL,
                PRICINGS.AREA_CODE,
                PRICINGS.AMOUNT,
                PRICINGS.VALID_FROM,
                PRICINGS.VALID_TO)
            .from(PRICINGS)
            .where(conditions)
            .orderBy(PRICINGS.VALID_FROM.asc())
            .limit(pageable.getPageSize())
            .offset((int) pageable.getOffset())
            .fetch(
                r -> {
                  @Nullable final Instant validTo =
                      r.get(PRICINGS.VALID_TO) != null
                          ? r.get(PRICINGS.VALID_TO).toInstant()
                          : null;
                  return new PricingSummaryDto(
                      r.get(PRICINGS.ID).toString(),
                      r.get(PRICINGS.PRODUCT_ID).toString(),
                      r.get(PRICINGS.LEVEL),
                      r.get(PRICINGS.AREA_CODE),
                      r.get(PRICINGS.AMOUNT),
                      r.get(PRICINGS.VALID_FROM).toInstant(),
                      validTo);
                });

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Optional<PricingDetailDto> findById(final String id) {
    final UUID uuid = UUID.fromString(id);
    return dsl.selectFrom(PRICINGS)
        .where(PRICINGS.ID.eq(uuid))
        .and(SoftDeleteCondition.notDeleted(PRICINGS))
        .fetchOptional()
        .map(
            r -> {
              @Nullable final Instant validTo = r.getValidTo() != null ? r.getValidTo().toInstant() : null;
              return new PricingDetailDto(
                  r.getId().toString(),
                  r.getProductId().toString(),
                  r.getLevel(),
                  r.getAreaCode(),
                  r.getAmount(),
                  r.getValidFrom().toInstant(),
                  validTo,
                  r.getVersion(),
                  r.getCreatedAt().toInstant(),
                  r.getUpdatedAt().toInstant());
            });
  }

  private List<Condition> buildConditions(final PricingListParam param) {
    final List<Condition> conditions = new ArrayList<>();
    conditions.add(SoftDeleteCondition.notDeleted(PRICINGS));

    if (param.productId() != null) {
      conditions.add(PRICINGS.PRODUCT_ID.eq(UUID.fromString(param.productId())));
    }
    if (param.level() != null) {
      conditions.add(PRICINGS.LEVEL.eq(param.level()));
    }
    if (param.areaCode() != null) {
      conditions.add(PRICINGS.AREA_CODE.eq(param.areaCode()));
    }
    if (param.asOf() != null) {
      final OffsetDateTime asOfOdt = param.asOf().atOffset(ZoneOffset.UTC);
      conditions.add(PRICINGS.VALID_FROM.lessOrEqual(asOfOdt));
      conditions.add(PRICINGS.VALID_TO.isNull().or(PRICINGS.VALID_TO.greaterThan(asOfOdt)));
    }
    return conditions;
  }
}
