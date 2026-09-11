import type { PaymentStatus } from "@/types/payment";
import { PAYMENT_STATUS_LABEL } from "@/types/payment";
import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const STATUS_STYLE: Record<PaymentStatus, string> = {
  PAGO: "bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300",
  PENDENTE: "bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300",
  REJEITADO: "bg-destructive/10 text-destructive",
  ESTORNADO: "bg-muted text-muted-foreground",
};

export function PaymentStatusBadge({ status }: { status: PaymentStatus }) {
  return (
    <Badge variant="outline" className={cn("border-transparent font-medium", STATUS_STYLE[status])}>
      {PAYMENT_STATUS_LABEL[status]}
    </Badge>
  );
}
