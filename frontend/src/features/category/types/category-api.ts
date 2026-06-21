import { apiClient } from "@/lib/api-client";
import type {
  CategoryDetail,
  CategorySummary,
  CreateCategoryInput,
  PageResponse,
  UpdateCategoryInput,
} from "@/features/category/types/category";

export function getCategories(params?: {
  page?: number;
  size?: number;
}): Promise<PageResponse<CategorySummary>> {
  const searchParams: Record<string, string> = {};
  if (params?.page !== undefined) {
    searchParams["page"] = String(params.page);
  }
  if (params?.size !== undefined) {
    searchParams["size"] = String(params.size);
  }
  return apiClient<PageResponse<CategorySummary>>({
    url: "/categories",
    method: "GET",
    params: searchParams,
  });
}

export function getCategory(id: string): Promise<CategoryDetail> {
  return apiClient<CategoryDetail>({
    url: `/categories/${id}`,
    method: "GET",
  });
}

export function getCategoryChildren(id: string): Promise<CategorySummary[]> {
  return apiClient<CategorySummary[]>({
    url: `/categories/${id}/children`,
    method: "GET",
  });
}

export function createCategory(data: CreateCategoryInput): Promise<void> {
  return apiClient<void>({
    url: "/categories",
    method: "POST",
    data,
  });
}

export function updateCategory(id: string, data: UpdateCategoryInput): Promise<void> {
  return apiClient<void>({
    url: `/categories/${id}`,
    method: "PUT",
    data,
  });
}

export function deleteCategory(id: string, version: number): Promise<void> {
  return apiClient<void>({
    url: `/categories/${id}`,
    method: "DELETE",
    data: { version },
  });
}
