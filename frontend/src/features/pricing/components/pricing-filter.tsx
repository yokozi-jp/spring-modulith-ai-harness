import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useProductOptions } from "@/features/pricing/hooks/use-product-options";
import { PRICING_LEVEL, PRICING_LEVEL_LABELS } from "@/features/pricing/types/pricing-level";

/** フィルタ未選択（すべて）を表す Select 用の値。 */
const ALL_VALUE = "__all__";

/** 価格一覧のフィルタ条件。 */
export interface PricingListFilterValues {
  readonly productId: string | undefined;
  readonly level: string | undefined;
}

interface PricingFilterProps {
  readonly filter: PricingListFilterValues;
  readonly onChange: (filter: PricingListFilterValues) => void;
}

/** 価格一覧の商品・レベルフィルタ。 */
export function PricingFilter({ filter, onChange }: PricingFilterProps) {
  const { options: productOptions } = useProductOptions();

  function handleProductChange(value: string) {
    const productId = value === ALL_VALUE ? undefined : value;
    onChange({ ...filter, productId });
  }

  function handleLevelChange(value: string) {
    const level = value === ALL_VALUE ? undefined : value;
    onChange({ ...filter, level });
  }

  return (
    <div className="flex gap-2">
      <Select value={filter.productId ?? ALL_VALUE} onValueChange={handleProductChange}>
        <SelectTrigger className="w-48">
          <SelectValue placeholder="商品で絞り込み" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value={ALL_VALUE}>すべての商品</SelectItem>
          {productOptions.map((option) => (
            <SelectItem key={option.id} value={option.id}>
              {option.name}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
      <Select value={filter.level ?? ALL_VALUE} onValueChange={handleLevelChange}>
        <SelectTrigger className="w-40">
          <SelectValue placeholder="レベルで絞り込み" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value={ALL_VALUE}>すべてのレベル</SelectItem>
          {Object.values(PRICING_LEVEL).map((level) => (
            <SelectItem key={level} value={level}>
              {PRICING_LEVEL_LABELS[level]}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}
