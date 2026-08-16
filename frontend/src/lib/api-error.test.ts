import { describe, expect, it } from "vite-plus/test";
import { ApiError, isApiError } from "@/lib/api-error";

describe("ApiError", () => {
  it("detail を優先してメッセージにする", () => {
    const error = new ApiError(409, "Conflict", "他のユーザーが先に更新しました");
    expect(error.message).toBe("他のユーザーが先に更新しました");
  });

  it("detail が空文字なら title をメッセージにする", () => {
    const error = new ApiError(500, "Internal Server Error", "");
    expect(error.message).toBe("Internal Server Error");
  });

  it("status, title, detail を保持する", () => {
    const error = new ApiError(404, "Not Found", "リソースが見つかりません");
    expect(error.status).toBe(404);
    expect(error.title).toBe("Not Found");
    expect(error.detail).toBe("リソースが見つかりません");
  });

  it("Error のインスタンスである", () => {
    const error = new ApiError(400, "Bad Request", "不正なリクエストです");
    expect(error).toBeInstanceOf(Error);
  });

  it("name が ApiError になる", () => {
    const error = new ApiError(400, "Bad Request", "不正なリクエストです");
    expect(error.name).toBe("ApiError");
  });
});

describe("isApiError", () => {
  it("ApiError インスタンスに対して true を返す", () => {
    const error = new ApiError(409, "Conflict", "競合が発生しました");
    expect(isApiError(error)).toBe(true);
  });

  it("通常の Error に対して false を返す", () => {
    expect(isApiError(new Error("通常のエラー"))).toBe(false);
  });

  it("null に対して false を返す", () => {
    expect(isApiError(null)).toBe(false);
  });

  it("プレーンオブジェクトに対して false を返す", () => {
    expect(isApiError({ status: 409, title: "Conflict", detail: "" })).toBe(false);
  });
});
