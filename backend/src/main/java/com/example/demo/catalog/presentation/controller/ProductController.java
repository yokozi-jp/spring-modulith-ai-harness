package com.example.demo.catalog.presentation.controller;

import com.example.demo.catalog.application.command.command.ArchiveProductCommand;
import com.example.demo.catalog.application.command.command.CreateProductCommand;
import com.example.demo.catalog.application.command.command.DeleteProductCommand;
import com.example.demo.catalog.application.command.command.PublishProductCommand;
import com.example.demo.catalog.application.command.command.UnpublishProductCommand;
import com.example.demo.catalog.application.command.command.UpdateProductCommand;
import com.example.demo.catalog.application.command.dto.CreatedProductDto;
import com.example.demo.catalog.application.command.handler.ProductCommandHandler;
import com.example.demo.catalog.application.query.param.ProductListParam;
import com.example.demo.catalog.application.query.service.ProductQueryService;
import com.example.demo.catalog.exception.ProductNotFoundException;
import com.example.demo.catalog.presentation.request.CreateProductRequest;
import com.example.demo.catalog.presentation.request.UpdateProductRequest;
import com.example.demo.catalog.presentation.response.ProductDetailResponse;
import com.example.demo.catalog.presentation.response.ProductSummaryResponse;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** 商品カタログ API コントローラ。 */
@Tag(name = "Product", description = "商品カタログ API")
@RestController
@RequestMapping("/products")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ProductController {

  /** コマンドハンドラ。 */
  private final ProductCommandHandler commandHandler;

  /** クエリサービス。 */
  private final ProductQueryService queryService;

  /** 商品を作成する。 */
  @Operation(summary = "商品を作成する")
  @ApiResponse(responseCode = "201", description = "作成成功")
  @PostMapping
  public ResponseEntity<Void> create(
      @RequestBody @Valid final CreateProductRequest request, final Principal principal) {
    final CreatedProductDto result =
        commandHandler.handle(
            new CreateProductCommand(
                request.name(),
                request.description(),
                request.categoryId(),
                request.sku(),
                principal.getName()));
    final URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(result.id())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  /** 商品一覧を取得する。 */
  @Operation(summary = "商品一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @GetMapping
  public Page<ProductSummaryResponse> list(final ProductListParam param, final Pageable pageable) {
    return queryService.findAll(param, pageable).map(ProductSummaryResponse::from);
  }

  /** 商品詳細を取得する。 */
  @Operation(summary = "商品詳細を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "404", description = "見つからない")
  @GetMapping("/{id}")
  public ProductDetailResponse findById(@PathVariable final String id) {
    return queryService
        .findById(id)
        .map(ProductDetailResponse::from)
        .orElseThrow(() -> new ProductNotFoundException(id));
  }

  /** 商品を更新する。 */
  @Operation(summary = "商品を更新する")
  @ApiResponse(responseCode = "200", description = "更新成功")
  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable final String id,
      @RequestBody @Valid final UpdateProductRequest request,
      final Principal principal) {
    commandHandler.handle(
        new UpdateProductCommand(
            id,
            request.name(),
            request.description(),
            request.categoryId(),
            request.version(),
            principal.getName()));
    return ResponseEntity.ok().build();
  }

  /** 商品を公開する。 */
  @Operation(summary = "商品を公開する")
  @ApiResponse(responseCode = "200", description = "公開成功")
  @PatchMapping("/{id}/publish")
  public ResponseEntity<Void> publish(
      @PathVariable final String id,
      @RequestBody final VersionRequest request,
      final Principal principal) {
    commandHandler.handle(new PublishProductCommand(id, request.version(), principal.getName()));
    return ResponseEntity.ok().build();
  }

  /** 商品を非公開にする。 */
  @Operation(summary = "商品を非公開にする")
  @ApiResponse(responseCode = "200", description = "非公開成功")
  @PatchMapping("/{id}/unpublish")
  public ResponseEntity<Void> unpublish(
      @PathVariable final String id,
      @RequestBody final VersionRequest request,
      final Principal principal) {
    commandHandler.handle(new UnpublishProductCommand(id, request.version(), principal.getName()));
    return ResponseEntity.ok().build();
  }

  /** 商品をアーカイブする。 */
  @Operation(summary = "商品をアーカイブする")
  @ApiResponse(responseCode = "200", description = "アーカイブ成功")
  @PatchMapping("/{id}/archive")
  public ResponseEntity<Void> archive(
      @PathVariable final String id,
      @RequestBody final VersionRequest request,
      final Principal principal) {
    commandHandler.handle(new ArchiveProductCommand(id, request.version(), principal.getName()));
    return ResponseEntity.ok().build();
  }

  /** 商品を削除する。 */
  @Operation(summary = "商品を削除する")
  @ApiResponse(responseCode = "204", description = "削除成功")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable final String id,
      @RequestBody final VersionRequest request,
      final Principal principal) {
    commandHandler.handle(new DeleteProductCommand(id, request.version(), principal.getName()));
    return ResponseEntity.noContent().build();
  }

  /** バージョン指定リクエスト。 */
  /* default */ record VersionRequest(int version) {}
}
