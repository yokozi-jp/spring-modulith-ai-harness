package com.example.demo.pricing.presentation.controller;

import com.example.demo.pricing.application.command.command.CreatePricingCommand;
import com.example.demo.pricing.application.command.command.DeletePricingCommand;
import com.example.demo.pricing.application.command.command.UpdatePricingCommand;
import com.example.demo.pricing.application.command.dto.CreatedPricingDto;
import com.example.demo.pricing.application.command.handler.PricingCommandHandler;
import com.example.demo.pricing.application.query.param.PricingListParam;
import com.example.demo.pricing.application.query.service.PricingQueryService;
import com.example.demo.pricing.exception.PricingNotFoundException;
import com.example.demo.pricing.presentation.request.CreatePricingRequest;
import com.example.demo.pricing.presentation.request.UpdatePricingRequest;
import com.example.demo.pricing.presentation.response.PricingDetailResponse;
import com.example.demo.pricing.presentation.response.PricingSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** 価格管理 API コントローラ。 */
@Tag(name = "Pricing", description = "価格管理 API")
@RestController
@RequestMapping("/pricings")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class PricingController {

  /** コマンドハンドラ。 */
  private final PricingCommandHandler commandHandler;

  /** クエリサービス。 */
  private final PricingQueryService queryService;

  /** 価格を作成する。 */
  @Operation(summary = "価格を作成する")
  @ApiResponse(responseCode = "201", description = "作成成功")
  @PostMapping
  public ResponseEntity<Void> create(
      @RequestBody @Valid final CreatePricingRequest request, final Principal principal) {
    final CreatedPricingDto result =
        commandHandler.handle(
            new CreatePricingCommand(
                request.productId(),
                request.level(),
                request.areaCode(),
                request.amount(),
                request.validFrom(),
                request.validTo(),
                principal.getName()));
    final URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(result.id())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  /** 価格一覧を取得する。 */
  @Operation(summary = "価格一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @GetMapping
  public Page<PricingSummaryResponse> list(final PricingListParam param, final Pageable pageable) {
    return queryService.findAll(param, pageable).map(PricingSummaryResponse::from);
  }

  /** 価格詳細を取得する。 */
  @Operation(summary = "価格詳細を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "404", description = "見つからない")
  @GetMapping("/{id}")
  public PricingDetailResponse findById(@PathVariable final String id) {
    return queryService
        .findById(id)
        .map(PricingDetailResponse::from)
        .orElseThrow(() -> new PricingNotFoundException(id));
  }

  /** 価格を更新する。 */
  @Operation(summary = "価格を更新する")
  @ApiResponse(responseCode = "200", description = "更新成功")
  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable final String id,
      @RequestBody @Valid final UpdatePricingRequest request,
      final Principal principal) {
    commandHandler.handle(
        new UpdatePricingCommand(
            id,
            request.amount(),
            request.validFrom(),
            request.validTo(),
            request.version(),
            principal.getName()));
    return ResponseEntity.ok().build();
  }

  /** 価格を削除する。 */
  @Operation(summary = "価格を削除する")
  @ApiResponse(responseCode = "204", description = "削除成功")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable final String id,
      @RequestBody @Valid final DeleteRequest request,
      final Principal principal) {
    commandHandler.handle(new DeletePricingCommand(id, request.version(), principal.getName()));
    return ResponseEntity.noContent().build();
  }

  /** 削除リクエスト（version のみ）。 */
  /* default */ record DeleteRequest(int version) {}
}
