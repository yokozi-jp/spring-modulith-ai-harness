package com.example.demo.catalog.presentation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.example.demo.catalog.application.command.dto.CreatedProductDto;
import com.example.demo.catalog.application.command.handler.ProductCommandHandler;
import com.example.demo.catalog.application.query.service.ProductQueryService;
import com.example.demo.config.WebMvcConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/** Controller tests for {@link ProductController}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {ProductController.class, ProductExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class ProductControllerTest {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  /** コマンドハンドラモック。 */
  @MockitoBean private ProductCommandHandler commandHandler;

  /** クエリサービスモック。 */
  @MockitoBean private ProductQueryService queryService;

  /** POST で 201 Created と Location ヘッダーを返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn201WithLocationOnCreate() throws Exception {
    Mockito.when(commandHandler.handle(Mockito.any())).thenReturn(new CreatedProductDto("new-id"));
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/products")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"test\",\"description\":\"desc\",\"categoryId\":\"c1\",\"sku\":\"SKU-1\"}"))
        .andExpect(MockMvcResultMatchers.status().isCreated())
        .andExpect(MockMvcResultMatchers.header().exists("Location"));
  }

  /** 存在しない ID で 404 を返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn404WhenNotFound() throws Exception {
    Mockito.when(queryService.findById("non-existent")).thenReturn(java.util.Optional.empty());
    mockMvc
        .perform(MockMvcRequestBuilders.get("/products/non-existent"))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }
}
