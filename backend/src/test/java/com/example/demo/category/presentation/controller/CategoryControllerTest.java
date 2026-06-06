package com.example.demo.category.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.category.application.command.command.CreateCategoryCommand;
import com.example.demo.category.application.command.dto.CreatedCategoryDto;
import com.example.demo.category.application.command.handler.CategoryCommandHandler;
import com.example.demo.category.application.query.dto.CategoryDetailDto;
import com.example.demo.category.application.query.dto.CategorySummaryDto;
import com.example.demo.category.application.query.service.CategoryQueryService;
import com.example.demo.config.WebMvcConfig;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/** Controller tests for {@link CategoryController}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {CategoryController.class, CategoryExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class CategoryControllerTest {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  /** コマンドハンドラモック。 */
  @MockitoBean private CategoryCommandHandler commandHandler;

  /** クエリサービスモック。 */
  @MockitoBean private CategoryQueryService queryService;

  /** 作成成功で 201 + Location ヘッダーを返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn201WithLocationOnCreate() throws Exception {
    when(commandHandler.handle(any(CreateCategoryCommand.class)))
        .thenReturn(new CreatedCategoryDto("new-id"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"家電\",\"sortOrder\":0}"))
        .andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.header().exists("Location"));
  }

  /** 一覧取得で 200 を返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn200OnList() throws Exception {
    final CategorySummaryDto dto = new CategorySummaryDto("cat-1", "家電", 0);
    when(queryService.findAll(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

    mockMvc.perform(MockMvcRequestBuilders.get("/categories")).andExpect(status().isOk());
  }

  /** 詳細取得で 200 を返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn200OnFindById() throws Exception {
    final CategoryDetailDto dto = new CategoryDetailDto("cat-1", "家電", 0, null, 1, List.of());
    when(queryService.findById("cat-1")).thenReturn(Optional.of(dto));

    mockMvc.perform(MockMvcRequestBuilders.get("/categories/cat-1")).andExpect(status().isOk());
  }

  /** 存在しない ID で 404 を返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn404WhenNotFound() throws Exception {
    when(queryService.findById("non-existent")).thenReturn(Optional.empty());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/categories/non-existent"))
        .andExpect(status().isNotFound());
  }
}
