export type PaymentStatus = "PENDENTE" | "PAGO" | "REJEITADO" | "ESTORNADO";

export interface PaymentDiscount {
  type: string;
  amount: number;
  judicial: boolean;
}

export interface Payment {
  id: string;
  beneficiaryCpf: string;
  beneficiaryName: string;
  competence: string;
  grossAmount: number;
  discounts: PaymentDiscount[];
  netAmount: number;
  status: PaymentStatus;
  issuedAt: string;
}

export const PAYMENT_STATUS_LABEL: Record<PaymentStatus, string> = {
  PENDENTE: "Pendente",
  PAGO: "Pago",
  REJEITADO: "Rejeitado",
  ESTORNADO: "Estornado",
};
