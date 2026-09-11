import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { EmptyState } from "@/components/states/empty-state";
import { ErrorState } from "@/components/states/error-state";
import { ApiError, getSocialPrograms } from "@/lib/api";
import { formatBRL } from "@/lib/format";

export default async function SocialProgramsPage() {
  try {
    const programs = await getSocialPrograms();
    return (
      <div className="flex flex-col gap-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Programas sociais</h1>
          <p className="text-muted-foreground">
            Parâmetros de elegibilidade e teto de renda por programa.
          </p>
        </div>
        {programs.length === 0 ? (
          <EmptyState message="Nenhum programa social cadastrado" />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Código</TableHead>
                <TableHead>Programa</TableHead>
                <TableHead className="text-right">Teto de renda per capita</TableHead>
                <TableHead className="text-right">Beneficiários</TableHead>
                <TableHead>Situação</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {programs.map((program) => (
                <TableRow key={program.code}>
                  <TableCell className="font-medium">{program.code}</TableCell>
                  <TableCell>{program.name}</TableCell>
                  <TableCell className="text-right">
                    {program.maxPerCapitaIncome > 0 ? formatBRL(program.maxPerCapitaIncome) : "Sem teto"}
                  </TableCell>
                  <TableCell className="text-right">{program.beneficiaryCount.toLocaleString("pt-BR")}</TableCell>
                  <TableCell>
                    <Badge variant={program.active ? "default" : "secondary"}>
                      {program.active ? "Ativo" : "Inativo"}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </div>
    );
  } catch (error) {
    const message = error instanceof ApiError ? error.message : "Erro inesperado ao carregar os programas.";
    return (
      <div className="flex flex-col gap-6">
        <h1 className="text-2xl font-bold tracking-tight">Programas sociais</h1>
        <ErrorState message={message} />
      </div>
    );
  }
}
