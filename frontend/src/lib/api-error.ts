/**
 * backend の RFC 9457 ProblemDetail レスポンスに対応する API エラー。
 *
 * `Error` を継承しているため、既存の `toError()` / `ErrorMessage`（`Error | null` を
 * 期待する実装）とそのまま互換性を持つ。HTTP ステータスコードでの分岐が必要な場合は
 * `isApiError()` で型ガードしてから `status` を参照する。
 */
export class ApiError extends Error {
  public readonly status: number;
  public readonly title: string;
  public readonly detail: string;

  public constructor(status: number, title: string, detail: string) {
    super(detail.length > 0 ? detail : title);
    this.name = "ApiError";
    this.status = status;
    this.title = title;
    this.detail = detail;
  }
}

/**
 * error が ApiError インスタンスかどうかを判定する型ガード。
 *
 * @example
 * if (isApiError(error) && error.status === 409) {
 *   // 楽観ロック競合の処理
 * }
 */
export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}
