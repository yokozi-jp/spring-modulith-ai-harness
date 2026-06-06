package com.example.demo.pricing.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.config.WebMvcConfig;
import com.example.demo.pricing.application.command.command.CreatePricingCommand;
import com.example.demo.pricing.application.command.dto.CreatedPricingDto;
import com.example.demo.pricing.application.command.handler.PricingCommandHandler;
import com.example.demo.pricing.application.query.dto.PricingDetailDto;
import com.example.demo.pricing.application.query.dto.PricingSummaryDto;
import com.example.demo.pricing.application.query.service.PricingQueryService;
import java.math.BigDecimal;
import java.time.Instant;
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

/** Controller tests for {@link PricingController}. */
@SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@WebMvcTest(
    controllers = {PricingController.class, PricingExceptionHandler.class},
    excludeFilters =
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfig.class))
class PricingControllerTest {

  /** MockMvc。 */
  private final MockMvc mockMvc;

  /** コマンドハンドラモック。 */
  @MockitoBean private PricingCommandHandler commandHandler;

  /** クエリサービスモック。 */
  @MockitoBean private PricingQueryService queryService;

  /** 作成成功で 201 + Location ヘッダーを返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn201WithLocationOnCreate() throws Exception {
    when(commandHandler.handle(any(CreatePricingCommand.class)))
        .thenReturn(new CreatedPricingDto("new-id"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/pricings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productId\":\"prod-1\",\"level\":\"REGION\",\"areaCode\":\"KANTO\","
                        + "\"amount\":1000,\"validFrom\":\"2025-01-01T00:00:00Z\"}"))
        .andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.header().exists("Location"));
  }

  /** 一覧取得で 200 を返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn200OnList() throws Exception {
    final Instant now = Instant.parse("2025-01-01T00:00:00Z");
    final PricingSummaryDto dto =
        new PricingSummaryDto("p-1", "prod-1", "REGION", "KANTO", BigDecimal.ONE, now, null);
    when(queryService.findAll(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

    mockMvc.perform(MockMvcRequestBuilders.get("/pricings")).andExpect(status().isOk());
  }

  /** 詳細取得で 200 を返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn200OnFindById() throws Exception {
    final Instant now = Instant.parse("2025-01-01T00:00:00Z");
    final PricingDetailDto dto =
        new PricingDetailDto(
            "p-1", "prod-1", "REGION", "KANTO", BigDecimal.ONE, now, null, 1, now, now);
    when(queryService.findById("p-1")).thenReturn(Optional.of(dto));

    mockMvc.perform(MockMvcRequestBuilders.get("/pricings/p-1")).andExpect(status().isOk());
  }

  /** 存在しない ID で 404 を返すこと。 */
  @Test
  @WithMockUser
  void shouldReturn404WhenNotFound() throws Exception {
    when(queryService.findById("non-existent")).thenReturn(Optional.empty());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/pricings/non-existent"))
        .andExpect(status().isNotFound());
  }
}
