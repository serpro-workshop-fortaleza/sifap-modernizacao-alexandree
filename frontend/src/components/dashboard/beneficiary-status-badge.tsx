import type { BeneficiaryStatus } from "@/types/beneficiary";
import { BENEFICIARY_STATUS_LABEL } from "@/types/beneficiary";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const STATUS_STYLE: Record<BeneficiaryStatus, string> = {
  A: "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300",
  S: "bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300",
  C: "bg-destructive/10 text-destructive",
  D: "bg-destructive/10 text-destructive",
  I: "bg-muted text-muted-foreground",
};

export function BeneficiaryStatusBadge({ status }: { status: BeneficiaryStatus }) {
  return (
    <Badge variant="outline" className={cn("border-transparent font-medium", STATUS_STYLE[status])}>
      {BENEFICIARY_STATUS_LABEL[status]}
    </Badge>
  );
}
