import { test as teardown } from "@playwright/test";

teardown("cleanup test data", async ({}) => {
  // TODO: 必要に応じてテストデータのクリーンアップを実装する
  // 現状はコンテナ破棄でリセットするため、ここでの処理は不要
});
