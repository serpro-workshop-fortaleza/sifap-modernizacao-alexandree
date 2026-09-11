import { getServerApiUrl } from "@/lib/env";
import type { Beneficiary } from "@/types/beneficiary";
import type { SocialProgram } from "@/types/social-program";
import type { Payment } from "@/types/payment";
import type { PayrollRunHistoryPoint, PayrollRunSummary } from "@/types/payroll";

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status?: number,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${getServerApiUrl()}${path}`;
  let response: Response;
  try {
    response = await fetch(url, {
      ...init,
      headers: { "Content-Type": "application/json", ...init?.headers },
      cache: "no-store",
    });
  } catch {
    throw new ApiError(`Não foi possível conectar ao backend em ${url}`);
  }
  if (!response.ok) {
    throw new ApiError(`Falha ao chamar ${path}: HTTP ${response.status}`, response.status);
  }
  return (await response.json()) as T;
}

export function getBeneficiaries(): Promise<Beneficiary[]> {
  return apiFetch<Beneficiary[]>("/api/v1/beneficiaries");
}

export function getSocialPrograms(): Promise<SocialProgram[]> {
  return apiFetch<SocialProgram[]>("/api/v1/social-programs");
}

export function getPayments(competence?: string): Promise<Payment[]> {
  const query = competence ? `?competence=${competence}` : "";
  return apiFetch<Payment[]>(`/api/v1/payments${query}`);
}

export function getPayrollSummary(competence?: string): Promise<PayrollRunSummary> {
  const query = competence ? `?competence=${competence}` : "";
  return apiFetch<PayrollRunSummary>(`/api/v1/payroll-runs/current${query}`);
}

export function getPayrollHistory(): Promise<PayrollRunHistoryPoint[]> {
  return apiFetch<PayrollRunHistoryPoint[]>("/api/v1/payroll-runs/history");
}

export function runPayroll(competence?: string): Promise<PayrollRunSummary> {
  return apiFetch<PayrollRunSummary>("/api/v1/payroll-runs", {
    method: "POST",
    body: JSON.stringify(competence ? { competence } : {}),
  });
}
