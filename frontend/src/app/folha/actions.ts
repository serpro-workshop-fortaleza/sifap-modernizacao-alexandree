"use server";

import { revalidatePath } from "next/cache";
import { getServerApiUrl } from "@/lib/env";
import type { PayrollRunSummary } from "@/types/payroll";

export interface RunPayrollResult {
  success: boolean;
  message: string;
  summary?: PayrollRunSummary;
}

export async function runPayrollAction(formData: FormData): Promise<RunPayrollResult> {
  const competence = formData.get("competence");
  const body = typeof competence === "string" && competence.trim() ? { competence } : {};

  let response: Response;
  try {
    response = await fetch(`${getServerApiUrl()}/api/v1/payroll-runs`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
      cache: "no-store",
    });
  } catch {
    return { success: false, message: "Não foi possível conectar ao backend para executar a folha." };
  }

  if (!response.ok) {
    return { success: false, message: `Falha ao executar a folha: HTTP ${response.status}` };
  }

  const summary = (await response.json()) as PayrollRunSummary;
  revalidatePath("/dashboard");
  revalidatePath("/pagamentos");
  revalidatePath("/folha");

  return { success: true, message: "Folha executada com sucesso.", summary };
}
