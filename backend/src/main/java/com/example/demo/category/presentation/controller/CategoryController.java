package com.example.demo.category.presentation.controller;

import com.example.demo.category.application.command.command.CreateCategoryCommand;
import com.example.demo.category.application.command.command.DeleteCategoryCommand;
import com.example.demo.category.application.command.command.MoveCategoryCommand;
import com.example.demo.category.application.command.command.UpdateCategoryCommand;
import com.example.demo.category.application.command.dto.CreatedCategoryDto;
import com.example.demo.category.application.command.handler.CategoryCommandHandler;
import com.example.demo.category.application.query.param.CategoryListParam;
import com.example.demo.category.application.query.service.CategoryQueryService;
import com.example.demo.category.exception.CategoryNotFoundException;
import com.example.demo.category.presentation.request.CreateCategoryRequest;
import com.example.demo.category.presentation.request.MoveCategoryRequest;
import com.example.demo.category.presentation.request.UpdateCategoryRequest;
import com.example.demo.category.presentation.response.CategoryDetailResponse;
import com.example.demo.category.presentation.response.CategorySummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
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

/** カテゴリ管理 API コントローラ。 */
@Tag(name = "Category", description = "カテゴリ管理 API")
@RestController
@RequestMapping("/categories")
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class CategoryController {

  /** コマンドハンドラ。 */
  private final CategoryCommandHandler commandHandler;

  /** クエリサービス。 */
  private final CategoryQueryService queryService;

  /** カテゴリを作成する。 */
  @Operation(summary = "カテゴリを作成する")
  @ApiResponse(responseCode = "201", description = "作成成功")
  @PostMapping
  public ResponseEntity<Void> create(
      @RequestBody @Valid final CreateCategoryRequest request, final Principal principal) {
    final CreatedCategoryDto result =
        commandHandler.handle(
            new CreateCategoryCommand(
                request.name(),
                request.sortOrder(),
                request.parentCategoryId(),
                principal.getName()));
    final URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(result.id())
            .toUri();
    return ResponseEntity.created(location).build();
  }

  /** ルートカテゴリ一覧を取得する。 */
  @Operation(summary = "ルートカテゴリ一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @GetMapping
  public Page<CategorySummaryResponse> list(
      final CategoryListParam param, final Pageable pageable) {
    return queryService.findAll(param, pageable).map(CategorySummaryResponse::from);
  }

  /** カテゴリ詳細を取得する。 */
  @Operation(summary = "カテゴリ詳細を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @ApiResponse(responseCode = "404", description = "見つからない")
  @GetMapping("/{id}")
  public CategoryDetailResponse findById(@PathVariable final String id) {
    return queryService
        .findById(id)
        .map(CategoryDetailResponse::from)
        .orElseThrow(() -> new CategoryNotFoundException(id));
  }

  /** 直接の子カテゴリ一覧を取得する。 */
  @Operation(summary = "直接の子カテゴリ一覧を取得する")
  @ApiResponse(responseCode = "200", description = "取得成功")
  @GetMapping("/{id}/children")
  public List<CategorySummaryResponse> findChildren(@PathVariable final String id) {
    return queryService.findChildrenById(id).stream().map(CategorySummaryResponse::from).toList();
  }

  /** カテゴリを更新する。 */
  @Operation(summary = "カテゴリを更新する")
  @ApiResponse(responseCode = "200", description = "更新成功")
  @PutMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable final String id,
      @RequestBody @Valid final UpdateCategoryRequest request,
      final Principal principal) {
    commandHandler.handle(
        new UpdateCategoryCommand(
            id, request.name(), request.sortOrder(), request.version(), principal.getName()));
    return ResponseEntity.ok().build();
  }

  /** カテゴリを移動する。 */
  @Operation(summary = "カテゴリを移動する")
  @ApiResponse(responseCode = "200", description = "移動成功")
  @PatchMapping("/{id}/move")
  public ResponseEntity<Void> move(
      @PathVariable final String id,
      @RequestBody @Valid final MoveCategoryRequest request,
      final Principal principal) {
    commandHandler.handle(
        new MoveCategoryCommand(
            id, request.newParentCategoryId(), request.version(), principal.getName()));
    return ResponseEntity.ok().build();
  }

  /** カテゴリを削除する。 */
  @Operation(summary = "カテゴリを削除する")
  @ApiResponse(responseCode = "204", description = "削除成功")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable final String id,
      @RequestBody @Valid final DeleteRequest request,
      final Principal principal) {
    commandHandler.handle(new DeleteCategoryCommand(id, request.version(), principal.getName()));
    return ResponseEntity.noContent().build();
  }

  /** 削除リクエスト（version のみ）。 */
  /* default */ record DeleteRequest(int version) {}
}
