import { RecentPaymentsTable } from "@/components/dashboard/recent-payments-table";
import { ErrorState } from "@/components/states/error-state";
import { ApiError, getPayments } from "@/lib/api";

export default async function PaymentsPage() {
  try {
    const payments = await getPayments();
    return (
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Pagamentos</h1>
          <p className="text-muted-foreground">
            Pagamentos gerados pela folha, com situação e valores líquidos.
          </p>
        </div>
        <div className="rounded-lg border bg-card p-6">
          <RecentPaymentsTable payments={payments} />
        </div>
      </div>
    );
  } catch (error) {
    const message = error instanceof ApiError ? error.message : "Erro inesperado ao carregar os pagamentos.";
    return (
      <div className="flex flex-col gap-6">
        <h1 className="text-2xl font-bold tracking-tight">Pagamentos</h1>
        <ErrorState message={message} />
      </div>
    );
  }
}
