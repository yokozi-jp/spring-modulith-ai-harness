package com.example.demo.catalog.presentation.controller;

import com.example.demo.catalog.application.command.handler.ProductCommandHandler;
import com.example.demo.catalog.application.query.service.ProductQueryService;
import com.example.demo.config.WebMvcConfig;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/** Security tests for {@link ProductController}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {ProductController.class, ProductExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class ProductControllerSecurityTest {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  /** コマンドハンドラモック。 */
  @MockitoBean private ProductCommandHandler commandHandler;

  /** クエリサービスモック。 */
  @MockitoBean private ProductQueryService queryService;

  /** 未認証でリダイレクトされること。 */
  @Test
  @WithAnonymousUser
  void shouldRedirectWhenUnauthenticated() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/products"))
        .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
  }

  /** CSRF トークンなしで 403 になること。 */
  @Test
  @WithMockUser
  void shouldReturn403WhenNoCsrfToken() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"name\":\"test\",\"description\":\"desc\",\"categoryId\":\"c1\",\"sku\":\"SKU-1\"}"))
        .andExpect(MockMvcResultMatchers.status().isForbidden());
  }
}
