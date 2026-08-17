import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useCategoryOptions } from "@/features/category/hooks/use-category-options";
import { PRODUCT_STATUS, PRODUCT_STATUS_LABELS } from "@/features/product/types/product-status";

/** フィルタ未選択（すべて）を表す Select 用の値。Radix Select は空文字を許容しないため専用値を使う。 */
const ALL_VALUE = "__all__";

/** 商品一覧のフィルタ条件。exactOptionalPropertyTypes 対応で undefined を許容する。 */
export interface ProductListFilterValues {
  readonly categoryId: string | undefined;
  readonly status: string | undefined;
}

interface ProductFilterProps {
  readonly filter: ProductListFilterValues;
  readonly onChange: (filter: ProductListFilterValues) => void;
}

/** 商品一覧のカテゴリ・ステータスフィルタ。 */
export function ProductFilter({ filter, onChange }: ProductFilterProps) {
  const { options } = useCategoryOptions();

  function handleCategoryChange(value: string) {
    const categoryId = value === ALL_VALUE ? undefined : value;
    onChange({ ...filter, categoryId });
  }

  function handleStatusChange(value: string) {
    const status = value === ALL_VALUE ? undefined : value;
    onChange({ ...filter, status });
  }

  return (
    <div className="flex gap-2">
      <Select value={filter.categoryId ?? ALL_VALUE} onValueChange={handleCategoryChange}>
        <SelectTrigger className="w-48">
          <SelectValue placeholder="カテゴリで絞り込み" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value={ALL_VALUE}>すべてのカテゴリ</SelectItem>
          {options.map((option) => (
            <SelectItem key={option.id} value={option.id}>
              {option.name}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Select value={filter.status ?? ALL_VALUE} onValueChange={handleStatusChange}>
        <SelectTrigger className="w-40">
          <SelectValue placeholder="ステータスで絞り込み" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value={ALL_VALUE}>すべてのステータス</SelectItem>
          {Object.values(PRODUCT_STATUS).map((status) => (
            <SelectItem key={status} value={status}>
              {PRODUCT_STATUS_LABELS[status]}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
