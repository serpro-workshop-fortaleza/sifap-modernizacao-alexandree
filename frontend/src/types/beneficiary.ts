export type BeneficiaryStatus = "A" | "S" | "C" | "D" | "I";

export interface Beneficiary {
  id: string;
  cpf: string;
  name: string;
  status: BeneficiaryStatus;
  socialProgramCode: string;
  householdIncome: number;
  registeredAt: string;
}

export const BENEFICIARY_STATUS_LABEL: Record<BeneficiaryStatus, string> = {
  A: "Ativo",
  S: "Suspenso",
  C: "Cancelado",
  D: "Desligado",
  I: "Inativo",
};
