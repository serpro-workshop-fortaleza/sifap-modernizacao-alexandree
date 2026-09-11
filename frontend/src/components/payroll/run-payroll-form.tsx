"use client";

import { useState, useTransition } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { formatBRL, formatCompetence } from "@/lib/format";
import type { PayrollRunSummary } from "@/types/payroll";
import { runPayrollAction } from "@/app/folha/actions";

const RETURN_CODE_LABEL: Record<PayrollRunSummary["returnCode"], string> = {
  0: "Concluída sem rejeições",
  4: "Concluída com rejeições",
  8: "Nenhum pagamento gerado",
  12: "Erro de execução",
};

export function RunPayrollForm({ initialSummary }: { initialSummary: PayrollRunSummary | null }) {
  const [isPending, startTransition] = useTransition();
  const [summary, setSummary] = useState<PayrollRunSummary | null>(initialSummary);

  function handleSubmit(formData: FormData) {
    startTransition(async () => {
      const result = await runPayrollAction(formData);
      if (result.success) {
        toast.success(result.message);
        setSummary(result.summary ?? null);
      } else {
        toast.error(result.message);
      }
    });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Executar folha da competência</CardTitle>
      </CardHeader>
      <form action={handleSubmit}>
        <CardContent className="flex flex-col gap-4">
          <Label className="flex flex-col gap-1 sm:max-w-xs">
            <span>Competência (AAAA-MM)</span>
            <Input name="competence" placeholder="Deixe em branco para o mês atual" pattern="\d{4}-\d{2}" />
          </Label>

          {summary && (
            <dl className="grid grid-cols-2 gap-4 rounded-md border p-4 text-sm sm:grid-cols-4">
              <div>
                <dt className="text-muted-foreground">Competência</dt>
                <dd className="font-medium">{formatCompetence(summary.competence)}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Pagamentos emitidos</dt>
                <dd className="font-medium">{summary.paymentsIssued.toLocaleString("pt-BR")}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Valor líquido</dt>
                <dd className="font-medium">{formatBRL(summary.totalNetAmount)}</dd>
              </div>
              <div>
                <dt className="text-muted-foreground">Resultado</dt>
                <dd className="font-medium">{RETURN_CODE_LABEL[summary.returnCode]}</dd>
              </div>
            </dl>
          )}
        </CardContent>
        <CardFooter>
          <Button type="submit" disabled={isPending} aria-busy={isPending}>
            {isPending ? "Executando…" : "Executar folha"}
          </Button>
        </CardFooter>
      </form>
    </Card>
  );
}
