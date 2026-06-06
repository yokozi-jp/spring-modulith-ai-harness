package com.example.demo.catalog.presentation.controller;

import com.example.demo.catalog.application.command.command.CreateProductCommand;
import com.example.demo.catalog.application.command.dto.CreatedProductDto;
import com.example.demo.catalog.application.command.handler.ProductCommandHandler;
import com.example.demo.catalog.application.query.param.ProductListParam;
import com.example.demo.catalog.application.query.service.ProductQueryService;
import com.example.demo.catalog.exception.ProductNotFoundException;
import com.example.demo.catalog.presentation.request.CreateProductRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Product コントローラ。 */
@Tag(name = "Product", description = "商品カタログ API")
@RestController
@RequestMapping("/products")
@Slf4j
@RequiredArgsConstructor
public class ProductController {

  /** コマンドハンドラ。 */
  private final ProductCommandHandler commandHandler;

  /** クエリサービス。 */
  private final ProductQueryService queryService;

  /** Product を作成する。 */
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

  /** Product 一覧を取得する。 */
  @Operation(summary = "商品一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @GetMapping
  public Page<ProductSummaryResponse> list(final ProductListParam param, final Pageable pageable) {
    return queryService.findAll(param, pageable).map(ProductSummaryResponse::from);
  }

  /** Product 詳細を取得する。 */
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
}
