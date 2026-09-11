import { BeneficiaryTable } from "@/components/beneficiaries/beneficiary-table";
import { ErrorState } from "@/components/states/error-state";
import { ApiError, getBeneficiaries } from "@/lib/api";

export default async function BeneficiariesPage() {
  try {
    const beneficiaries = await getBeneficiaries();
    return (
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Beneficiários</h1>
          <p className="text-muted-foreground">
            Situação cadastral e vínculo com programas sociais.
          </p>
        </div>
        <BeneficiaryTable beneficiaries={beneficiaries} />
      </div>
    );
  } catch (error) {
    const message = error instanceof ApiError ? error.message : "Erro inesperado ao carregar os beneficiários.";
    return (
      <div className="flex flex-col gap-6">
        <h1 className="text-2xl font-bold tracking-tight">Beneficiários</h1>
        <ErrorState message={message} />
      </div>
    );
  }
}
