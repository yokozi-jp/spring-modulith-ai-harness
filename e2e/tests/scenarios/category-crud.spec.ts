import { test, expect } from "@playwright/test";
import { CategoryListPage } from "../../pages/category-list-page";
import { CategoryCreatePage } from "../../pages/category-create-page";

/**
 * カテゴリ CRUD シナリオテスト。
 *
 * 前提:
 *   - global.setup.ts で「食品」「飲料」の2件がシード済み
 *   - global.setup.ts で ACCESS_TOKEN を取得済み
 *
 * テスト対象:
 *   - 一覧表示（シードデータの確認）
 *   - 新規作成 → 一覧に反映
 */
test.describe("Category CRUD", () => {
  test("seeded categories are displayed in the list", async ({ page }) => {
    const listPage = new CategoryListPage(page);
    await listPage.navigate();

    // テーブルが表示されること
    await expect(listPage.isTableVisible()).resolves.toBe(true);

    // シードデータの2件が表示されること
    const names = await listPage.getCategoryNames();
    expect(names).toContain("食品");
    expect(names).toContain("飲料");
  });

  test("empty state is shown when no categories exist", async ({ page }) => {
    // NOTE: このテストはシードデータがある状態では失敗する。
    // 独立性のため、API 経由で全削除するか、別の setup を使う必要がある。
    // 現段階ではスキップし、構成確認のサンプルとして残す。
    test.skip();

    const listPage = new CategoryListPage(page);
    await listPage.navigate();
    await expect(listPage.isEmptyStateVisible()).resolves.toBe(true);
  });

  test("can create a new category and see it in the list", async ({
    page,
  }) => {
    const createPage = new CategoryCreatePage(page);
    await createPage.navigate();

    // フォーム入力
    await createPage.fillName("お菓子");
    await createPage.fillSortOrder(3);

    // 送信
    await createPage.submit();

    // 成功後、一覧ページにリダイレクト
    await createPage.waitForRedirectToList();

    // 一覧に新しいカテゴリが表示される
    const listPage = new CategoryListPage(page);
    const names = await listPage.getCategoryNames();
    expect(names).toContain("お菓子");
  });

  test("navigating from list to create page works", async ({ page }) => {
    const listPage = new CategoryListPage(page);
    await listPage.navigate();

    await listPage.clickCreateLink();

    // 作成ページに遷移したことを確認
    await expect(page).toHaveURL(/categories-new\.html/);
  });
});

test.describe("Category API", () => {
  const backendURL = process.env.BACKEND_URL ?? "http://backend:18080";

  test("GET /api/v1/categories returns seeded data", async ({ request }) => {
    const response = await request.get(
      `${backendURL}/api/v1/categories?page=0&size=50`,
      {
        headers: {
          Authorization: `Bearer ${process.env.ACCESS_TOKEN}`,
        },
      },
    );

    expect(response.status()).toBe(200);

    const data = await response.json();
    expect(data.content.length).toBeGreaterThanOrEqual(2);

    const names = data.content.map(
      (c: { name: string }) => c.name,
    ) as string[];
    expect(names).toContain("食品");
    expect(names).toContain("飲料");
  });

  test("POST /api/v1/categories creates and returns 201 with Location", async ({
    request,
  }) => {
    const response = await request.post(`${backendURL}/api/v1/categories`, {
      headers: {
        Authorization: `Bearer ${process.env.ACCESS_TOKEN}`,
        "Content-Type": "application/json",
      },
      data: {
        name: "調味料",
        sortOrder: 10,
        parentCategoryId: null,
      },
    });

    expect(response.status()).toBe(201);
    expect(response.headers()["location"]).toBeDefined();
  });
});
