import { RunPayrollForm } from "@/components/payroll/run-payroll-form";
import { ApiError, getPayrollSummary } from "@/lib/api";
import type { PayrollRunSummary } from "@/types/payroll";

export default async function PayrollPage() {
  let summary: PayrollRunSummary | null = null;
  let errorMessage: string | null = null;

  try {
    summary = await getPayrollSummary();
  } catch (error) {
    errorMessage = error instanceof ApiError ? error.message : "Erro inesperado ao carregar a folha atual.";
  }

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Folha mensal</h1>
        <p className="text-muted-foreground">
          Gere a folha de pagamento de uma competência e acompanhe o resultado.
        </p>
      </div>
      {errorMessage && (
        <p role="status" className="text-sm text-muted-foreground">
          {errorMessage} A folha ainda pode ser executada abaixo.
        </p>
      )}
      <RunPayrollForm initialSummary={summary} />
    </div>
  );
}
