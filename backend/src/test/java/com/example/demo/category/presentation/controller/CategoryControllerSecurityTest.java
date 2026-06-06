package com.example.demo.category.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.category.application.command.handler.CategoryCommandHandler;
import com.example.demo.category.application.query.service.CategoryQueryService;
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

/** Security tests for {@link CategoryController}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {CategoryController.class, CategoryExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class CategoryControllerSecurityTest {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  /** コマンドハンドラモック。 */
  @MockitoBean private CategoryCommandHandler commandHandler;

  /** クエリサービスモック。 */
  @MockitoBean private CategoryQueryService queryService;

  /** 未認証でリダイレクトされること。 */
  @Test
  @WithAnonymousUser
  void shouldRedirectWhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/categories")).andExpect(status().is3xxRedirection());
  }

  /** CSRF トークンなしで 403 になること。 */
  @Test
  @WithMockUser
  void shouldReturn403WhenNoCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"家電\",\"sortOrder\":0}"))
        .andExpect(status().isForbidden());
  }
}
