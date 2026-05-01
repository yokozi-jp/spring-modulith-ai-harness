# Basic 認証チェーンで flushBuffer による EntryPoint 強制

Spring Boot 4 + Spring Security 7 で複数の `SecurityFilterChain`（Basic 認証 + OAuth2 Login）を構成した際、Basic 認証チェーンの未認証レスポンスが 401 ではなく 302（OAuth2 リダイレクト）になる問題が発生した。`securityMatcher` でチェーンを分離し、`httpBasic` や `exceptionHandling` で `BasicAuthenticationEntryPoint` を明示設定しても、OAuth2 Client の自動設定がグローバルに `AuthenticationEntryPoint` を上書きするため解決しなかった。

`response.setStatus(401)` + `response.flushBuffer()` でレスポンスを即座にコミットし、後続フィルターによる 302 への上書きを防ぐワークアラウンドを採用した。

## Considered Options

- `exceptionHandling(ex -> ex.authenticationEntryPoint(new BasicAuthenticationEntryPoint()))` — OAuth2 の EntryPoint に上書きされ効果なし
- `HttpStatusEntryPoint(UNAUTHORIZED)` — 401 は返るが `WWW-Authenticate` ヘッダーがなくブラウザの Basic 認証ダイアログが出ない
- `oauth2Login(AbstractHttpConfigurer::disable)` + `oauth2Client(AbstractHttpConfigurer::disable)` — 無効化しても 302 が返り続けた
- `sessionManagement(STATELESS)` — 単独では効果なし
- `response.flushBuffer()` — レスポンスをコミットして上書きを防止。動作する

## Consequences

- Spring Security 7 の正式なマルチチェーン構成のドキュメントやバグ修正が出た場合、`flushBuffer` を除去して標準的な設定に置き換えるべき
- `flushBuffer` 後はエラーボディを書き込めないため、Basic 認証の 401 レスポンスにボディがない（ブラウザの Basic 認証ダイアログ表示には影響しない）
