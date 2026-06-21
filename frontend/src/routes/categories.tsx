import { createFileRoute } from "@tanstack/react-router";
import { CategoryListPage } from "@/features/category/components/category-list-page";

export const Route = createFileRoute("/categories")({
  component: CategoryListPage,
});
