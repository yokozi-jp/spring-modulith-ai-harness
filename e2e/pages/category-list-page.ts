import type { Page } from "@playwright/test";

/**
 * カテゴリ一覧ページの Page Object Model。
 * e2e/frontend-stub/categories.html に対応する。
 */
export class CategoryListPage {
  constructor(private readonly page: Page) {}

  /** 一覧ページに遷移する。 */
  async navigate(): Promise<void> {
    await this.page.goto("/categories.html");
  }

  /** 「新規作成」リンクをクリックする。 */
  async clickCreateLink(): Promise<void> {
    await this.page.getByRole("link", { name: "新規作成" }).click();
  }

  /** カテゴリ名の一覧を取得する。 */
  async getCategoryNames(): Promise<string[]> {
    await this.page.waitForSelector("#category-table, #empty-state");
    const rows = this.page.locator("#category-body tr td:first-child");
    return rows.allTextContents();
  }

  /** 空状態メッセージが表示されているか。 */
  async isEmptyStateVisible(): Promise<boolean> {
    return this.page.locator("#empty-state").isVisible();
  }

  /** テーブルが表示されているか。 */
  async isTableVisible(): Promise<boolean> {
    return this.page.locator("#category-table").isVisible();
  }

  /** エラーメッセージを取得する。 */
  async getErrorMessage(): Promise<string | null> {
    const el = this.page.locator("#error");
    if (await el.isVisible()) {
      return el.textContent();
    }
    return null;
  }
}
