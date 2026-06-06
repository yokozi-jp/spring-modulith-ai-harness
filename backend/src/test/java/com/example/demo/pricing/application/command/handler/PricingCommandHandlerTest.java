package com.example.demo.pricing.application.command.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.OptimisticLockException;
import com.example.demo.pricing.application.command.command.CreatePricingCommand;
import com.example.demo.pricing.application.command.command.DeletePricingCommand;
import com.example.demo.pricing.application.command.command.UpdatePricingCommand;
import com.example.demo.pricing.application.command.dto.CreatedPricingDto;
import com.example.demo.pricing.domain.model.aggregate.Pricing;
import com.example.demo.pricing.domain.model.valueobject.Price;
import com.example.demo.pricing.domain.model.valueobject.PricingLevel;
import com.example.demo.pricing.domain.model.valueobject.identifier.PricingId;
import com.example.demo.pricing.domain.repository.PricingRepository;
import com.example.demo.pricing.domain.service.PricingFactory;
import com.example.demo.pricing.exception.PricingNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link PricingCommandHandler}. */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@ExtendWith(MockitoExtension.class)
class PricingCommandHandlerTest {

  /** テスト用固定時刻。 */
  private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

  /** テスト用有効終了日時。 */
  private static final Instant VALID_TO = Instant.parse("2025-12-31T23:59:59Z");

  /** テスト用商品 ID。 */
  private static final String PRODUCT_ID = "product-1";

  /** テスト用オペレーター ID。 */
  private static final String OPERATOR_ID = "operator-1";

  /** テスト用価格 ID。 */
  private static final String PRICING_ID = "pricing-1";

  /** ファクトリモック。 */
  @Mock private PricingFactory factory;

  /** リポジトリモック。 */
  @Mock private PricingRepository repository;

  /** テスト対象。 */
  @InjectMocks private PricingCommandHandler sut;

  /** 作成コマンドを処理して ID を返すこと。 */
  @Test
  void shouldCreatePricingAndReturnId() {
    final Pricing pricing = createTestPricing();
    when(factory.create(
            PRODUCT_ID,
            PricingLevel.REGION,
            "KANTO",
            new Price(new BigDecimal("1000")),
            NOW,
            VALID_TO))
        .thenReturn(pricing);
    when(repository.existsOverlapping(pricing)).thenReturn(false);

    final CreatePricingCommand command =
        new CreatePricingCommand(
            PRODUCT_ID, "REGION", "KANTO", new BigDecimal("1000"), NOW, VALID_TO, OPERATOR_ID);
    final CreatedPricingDto result = sut.handle(command);

    assertEquals(PRICING_ID, result.id(), "id should match");
    org.mockito.Mockito.verify(repository).save(pricing, 0, OPERATOR_ID);
  }

  /** 期間重複がある場合に作成が失敗すること。 */
  @Test
  void shouldThrowWhenOverlappingOnCreate() {
    final Pricing pricing = createTestPricing();
    when(factory.create(
            PRODUCT_ID,
            PricingLevel.REGION,
            "KANTO",
            new Price(new BigDecimal("1000")),
            NOW,
            VALID_TO))
        .thenReturn(pricing);
    when(repository.existsOverlapping(pricing)).thenReturn(true);

    final CreatePricingCommand command =
        new CreatePricingCommand(
            PRODUCT_ID, "REGION", "KANTO", new BigDecimal("1000"), NOW, VALID_TO, OPERATOR_ID);

    assertThrows(IllegalStateException.class, () -> sut.handle(command), "should throw on overlap");
  }

  /** 更新コマンドを処理すること。 */
  @Test
  void shouldUpdatePricing() {
    final Pricing pricing = createTestPricing();
    final PricingId id = new PricingId(PRICING_ID);
    when(repository.findById(id)).thenReturn(Optional.of(pricing));
    when(repository.getVersion(id)).thenReturn(1);
    when(repository.existsOverlapping(any(Pricing.class))).thenReturn(false);

    final UpdatePricingCommand command =
        new UpdatePricingCommand(PRICING_ID, new BigDecimal("2000"), NOW, VALID_TO, 1, OPERATOR_ID);
    sut.handle(command);

    org.mockito.Mockito.verify(repository)
        .save(
            any(Pricing.class),
            org.mockito.ArgumentMatchers.eq(1),
            org.mockito.ArgumentMatchers.eq(OPERATOR_ID));
  }

  /** 更新時にバージョン不一致で失敗すること。 */
  @Test
  void shouldThrowOptimisticLockOnUpdate() {
    final Pricing pricing = createTestPricing();
    final PricingId id = new PricingId(PRICING_ID);
    when(repository.findById(id)).thenReturn(Optional.of(pricing));
    when(repository.getVersion(id)).thenReturn(2);

    final UpdatePricingCommand command =
        new UpdatePricingCommand(PRICING_ID, new BigDecimal("2000"), NOW, VALID_TO, 1, OPERATOR_ID);

    assertThrows(
        OptimisticLockException.class,
        () -> sut.handle(command),
        "should throw on version mismatch");
  }

  /** 更新時に存在しない場合に失敗すること。 */
  @Test
  void shouldThrowNotFoundOnUpdate() {
    final PricingId id = new PricingId(PRICING_ID);
    when(repository.findById(id)).thenReturn(Optional.empty());

    final UpdatePricingCommand command =
        new UpdatePricingCommand(PRICING_ID, new BigDecimal("2000"), NOW, VALID_TO, 1, OPERATOR_ID);

    assertThrows(
        PricingNotFoundException.class, () -> sut.handle(command), "should throw when not found");
  }

  /** 削除コマンドを処理すること。 */
  @Test
  void shouldDeletePricing() {
    final Pricing pricing = createTestPricing();
    final PricingId id = new PricingId(PRICING_ID);
    when(repository.findById(id)).thenReturn(Optional.of(pricing));
    when(repository.getVersion(id)).thenReturn(1);

    final DeletePricingCommand command = new DeletePricingCommand(PRICING_ID, 1, OPERATOR_ID);
    sut.handle(command);

    org.mockito.Mockito.verify(repository).delete(id, 1, OPERATOR_ID);
  }

  /** 削除時に存在しない場合に失敗すること。 */
  @Test
  void shouldThrowNotFoundOnDelete() {
    final PricingId id = new PricingId(PRICING_ID);
    when(repository.findById(id)).thenReturn(Optional.empty());

    final DeletePricingCommand command = new DeletePricingCommand(PRICING_ID, 1, OPERATOR_ID);

    assertThrows(
        PricingNotFoundException.class, () -> sut.handle(command), "should throw when not found");
  }

  /** 削除時にバージョン不一致で失敗すること。 */
  @Test
  void shouldThrowOptimisticLockOnDelete() {
    final Pricing pricing = createTestPricing();
    final PricingId id = new PricingId(PRICING_ID);
    when(repository.findById(id)).thenReturn(Optional.of(pricing));
    when(repository.getVersion(id)).thenReturn(2);

    final DeletePricingCommand command = new DeletePricingCommand(PRICING_ID, 1, OPERATOR_ID);

    assertThrows(
        OptimisticLockException.class,
        () -> sut.handle(command),
        "should throw on version mismatch");
  }

  private static Pricing createTestPricing() {
    return Pricing.reconstitute(
        new PricingId(PRICING_ID),
        PRODUCT_ID,
        PricingLevel.REGION,
        "KANTO",
        new Price(new BigDecimal("1000")),
        NOW,
        VALID_TO);
  }
}
