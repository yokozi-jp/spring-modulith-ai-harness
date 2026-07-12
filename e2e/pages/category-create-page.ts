import type { Page } from "@playwright/test";

/**
 * カテゴリ作成ページの Page Object Model。
 * e2e/frontend-stub/categories-new.html に対応する。
 */
export class CategoryCreatePage {
  constructor(private readonly page: Page) {}

  /** 作成ページに遷移する。 */
  async navigate(): Promise<void> {
    await this.page.goto("/categories-new.html");
  }

  /** カテゴリ名を入力する。 */
  async fillName(name: string): Promise<void> {
    await this.page.getByLabel("カテゴリ名").fill(name);
  }

  /** 並び順を入力する。 */
  async fillSortOrder(order: number): Promise<void> {
    await this.page.getByLabel("並び順").fill(String(order));
  }

  /** 作成ボタンをクリックする。 */
  async submit(): Promise<void> {
    await this.page.getByRole("button", { name: "作成" }).click();
  }

  /** 成功メッセージが表示されるのを待つ。 */
  async waitForSuccess(): Promise<string | null> {
    const el = this.page.locator("#success");
    await el.waitFor({ state: "visible", timeout: 5000 });
    return el.textContent();
  }

  /** エラーメッセージを取得する。 */
  async getErrorMessage(): Promise<string | null> {
    const el = this.page.locator("#error");
    if (await el.isVisible()) {
      return el.textContent();
    }
    return null;
  }

  /** 一覧ページへのリダイレクトを待つ。 */
  async waitForRedirectToList(): Promise<void> {
    await this.page.waitForURL("**/categories.html", { timeout: 5000 });
  }
}
