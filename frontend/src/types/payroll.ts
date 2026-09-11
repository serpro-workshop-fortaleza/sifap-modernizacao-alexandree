export interface PayrollRunSummary {
  competence: string;
  paymentsIssued: number;
  paymentsRejected: number;
  ignoredBeneficiaries: number;
  totalGrossAmount: number;
  totalNetAmount: number;
  returnCode: 0 | 4 | 8 | 12;
  finishedAt: string | null;
}

export interface PayrollRunHistoryPoint {
  competence: string;
  totalNetAmount: number;
  paymentsIssued: number;
}
