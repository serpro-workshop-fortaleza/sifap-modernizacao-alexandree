import { Banknote, Users, AlertCircle, ClipboardCheck } from "lucide-react";
import { StatCard } from "@/components/dashboard/stat-card";
import { PayrollHistoryChart } from "@/components/dashboard/payroll-history-chart";
import { RecentPaymentsTable } from "@/components/dashboard/recent-payments-table";
import { ErrorState } from "@/components/states/error-state";
import { ApiError, getPayments, getPayrollHistory, getPayrollSummary } from "@/lib/api";
import { formatBRL, formatCompetence } from "@/lib/format";

export default async function DashboardPage() {
  try {
    const [summary, history, payments] = await Promise.all([
      getPayrollSummary(),
      getPayrollHistory(),
      getPayments(),
    ]);

    const recentPayments = payments.slice(0, 8);

    return (
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Painel da folha de pagamento</h1>
          <p className="text-muted-foreground">
            Competência atual: {formatCompetence(summary.competence)}
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <StatCard
            title="Pagamentos emitidos"
            value={summary.paymentsIssued.toLocaleString("pt-BR")}
            icon={ClipboardCheck}
          />
          <StatCard
            title="Valor líquido da competência"
            value={formatBRL(summary.totalNetAmount)}
            icon={Banknote}
          />
          <StatCard
            title="Beneficiários ignorados"
            value={summary.ignoredBeneficiaries.toLocaleString("pt-BR")}
            icon={Users}
          />
          <StatCard
            title="Pagamentos rejeitados"
            value={summary.paymentsRejected.toLocaleString("pt-BR")}
            icon={AlertCircle}
            trend={summary.paymentsRejected > 0 ? "Requer atenção" : "Nenhuma rejeição"}
            trendPositive={summary.paymentsRejected === 0}
          />
        </div>

        <div className="grid gap-4 lg:grid-cols-3">
          <PayrollHistoryChart data={history} />
          <div className="col-span-full rounded-lg border bg-card p-6 lg:col-span-1">
            <h2 className="mb-4 font-semibold">Últimos pagamentos</h2>
            <RecentPaymentsTable payments={recentPayments} />
          </div>
        </div>
      </div>
    );
  } catch (error) {
    const message = error instanceof ApiError ? error.message : "Erro inesperado ao carregar o painel.";
    return (
      <div className="flex flex-col gap-6">
        <h1 className="text-2xl font-bold tracking-tight">Painel da folha de pagamento</h1>
        <ErrorState message={message} />
      </div>
    );
  }
}
