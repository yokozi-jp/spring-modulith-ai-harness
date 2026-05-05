package com.example.demo.security.actuator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Basic 認証用の {@link AuthenticationEntryPoint}。レスポンスを即座にコミットして後続フィルターによる上書きを防ぐ。
 *
 * <p>Spring Security 7 で複数の {@code SecurityFilterChain}（Basic 認証 + OAuth2 Login）を構成した際、 OAuth2
 * Client の自動設定がグローバルに {@code AuthenticationEntryPoint} を上書きし、Basic 認証チェーンの未認証レスポンスが 401 ではなく
 * 302（OAuth2 リダイレクト）になる問題のワークアラウンド。
 *
 * <p>{@code response.flushBuffer()} でレスポンスをコミットし、後続フィルターによる 302 への上書きを防止する。
 *
 * <p>Spring Security 7 の正式なマルチチェーン構成のバグ修正が出た場合、本クラスを削除して標準的な {@code BasicAuthenticationEntryPoint}
 * に置き換えること。
 *
 * @see <a
 *     href="../../../../../../../docs/adr/0004-basic-auth-entrypoint-flush-workaround.md">ADR-0004</a>
 */
final class FlushingBasicAuthEntryPoint implements AuthenticationEntryPoint {

  /** WWW-Authenticate ヘッダーの realm 値。 */
  private final String realm;

  /* default */ FlushingBasicAuthEntryPoint(final String realm) {
    this.realm = realm;
  }

  @Override
  public void commence(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
    response.flushBuffer();
  }
}
