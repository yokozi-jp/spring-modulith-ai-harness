package com.example.demo.pricing.presentation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.WebMvcConfig;
import com.example.demo.pricing.application.command.handler.PricingCommandHandler;
import com.example.demo.pricing.application.query.service.PricingQueryService;
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

/** Security tests for {@link PricingController}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {PricingController.class, PricingExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class PricingControllerSecurityTest {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  /** コマンドハンドラモック。 */
  @MockitoBean private PricingCommandHandler commandHandler;

  /** クエリサービスモック。 */
  @MockitoBean private PricingQueryService queryService;

  /** 未認証でリダイレクトされること。 */
  @Test
  @WithAnonymousUser
  void shouldRedirectWhenUnauthenticated() throws Exception {
    mockMvc.perform(get("/pricings")).andExpect(status().is3xxRedirection());
  }

  /** CSRF トークンなしで 403 になること。 */
  @Test
  @WithMockUser
  void shouldReturn403WhenNoCsrfToken() throws Exception {
    mockMvc
        .perform(
            post("/pricings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productId\":\"prod-1\",\"level\":\"REGION\",\"areaCode\":\"KANTO\","
                        + "\"amount\":1000,\"validFrom\":\"2025-01-01T00:00:00Z\"}"))
        .andExpect(status().isForbidden());
  }
}
