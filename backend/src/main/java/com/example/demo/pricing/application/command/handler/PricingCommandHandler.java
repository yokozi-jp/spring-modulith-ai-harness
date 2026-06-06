package com.example.demo.pricing.application.command.handler;

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
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Pricing コマンドハンドラ。 */
@RequiredArgsConstructor
@Component
public class PricingCommandHandler {

  /** ファクトリ。 */
  private final PricingFactory factory;

  /** リポジトリ。 */
  private final PricingRepository repository;

  /** 作成コマンドを処理する。 */
  @Transactional
  @CommandHandler
  public CreatedPricingDto handle(final CreatePricingCommand command) {
    final PricingLevel level = PricingLevel.valueOf(command.level());
    final Price amount = new Price(command.amount());
    final Pricing pricing =
        factory.create(
            command.productId(),
            level,
            command.areaCode(),
            amount,
            command.validFrom(),
            command.validTo());
    if (repository.existsOverlapping(pricing)) {
      throw new IllegalStateException("Overlapping pricing period exists");
    }
    repository.save(pricing, 0, command.operatorId());
    return new CreatedPricingDto(pricing.getId().value());
  }

  /** 更新コマンドを処理する。 */
  @Transactional
  @CommandHandler
  public void handle(final UpdatePricingCommand command) {
    final PricingId id = new PricingId(command.id());
    final Pricing pricing =
        repository.findById(id).orElseThrow(() -> new PricingNotFoundException(command.id()));
    verifyVersion(id, command.version());
    final Price newAmount = new Price(command.amount());
    final Pricing updated = pricing.update(newAmount, command.validFrom(), command.validTo());
    if (repository.existsOverlapping(updated)) {
      throw new IllegalStateException("Overlapping pricing period exists");
    }
    repository.save(updated, command.version(), command.operatorId());
  }

  /** 削除コマンドを処理する。 */
  @Transactional
  @CommandHandler
  public void handle(final DeletePricingCommand command) {
    final PricingId id = new PricingId(command.id());
    if (repository.findById(id).isEmpty()) {
      throw new PricingNotFoundException(command.id());
    }
    verifyVersion(id, command.version());
    repository.delete(id, command.version(), command.operatorId());
  }

  private void verifyVersion(final PricingId id, final int expectedVersion) {
    final int currentVersion = repository.getVersion(id);
    if (currentVersion != expectedVersion) {
      throw new OptimisticLockException("Pricing", id.value());
    }
  }
}
