import { createFileRoute } from "@tanstack/react-router";
import { CategoryCreatePage } from "@/features/category/components/category-create-page";

export const Route = createFileRoute("/categories_/new")({
  component: CategoryCreatePage,
});
