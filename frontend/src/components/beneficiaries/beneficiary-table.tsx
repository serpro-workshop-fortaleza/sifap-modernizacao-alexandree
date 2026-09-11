"use client";

import { useMemo, useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { BeneficiaryStatusBadge } from "@/components/dashboard/beneficiary-status-badge";
import { EmptyState } from "@/components/states/empty-state";
import { formatBRL, formatCpf, formatDateBR } from "@/lib/format";
import type { Beneficiary } from "@/types/beneficiary";

export function BeneficiaryTable({ beneficiaries }: { beneficiaries: Beneficiary[] }) {
  const [term, setTerm] = useState("");

  const filtered = useMemo(() => {
    const normalized = term.trim().toLowerCase();
    if (!normalized) return beneficiaries;
    return beneficiaries.filter(
      (beneficiary) =>
        beneficiary.name.toLowerCase().includes(normalized) ||
        beneficiary.cpf.replace(/\D/g, "").includes(normalized.replace(/\D/g, "")),
    );
  }, [beneficiaries, term]);

  return (
    <div className="flex flex-col gap-4">
      <Label className="flex flex-col gap-1 sm:max-w-xs">
        <span>Filtrar por nome ou CPF</span>
        <Input
          value={term}
          onChange={(event) => setTerm(event.target.value)}
          placeholder="Digite o nome ou o CPF"
        />
      </Label>

      {filtered.length === 0 ? (
        <EmptyState message="Nenhum beneficiário encontrado para o filtro informado" />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nome</TableHead>
              <TableHead>CPF</TableHead>
              <TableHead>Programa</TableHead>
              <TableHead className="text-right">Renda familiar</TableHead>
              <TableHead>Situação</TableHead>
              <TableHead>Cadastrado em</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.map((beneficiary) => (
              <TableRow key={beneficiary.id}>
                <TableCell className="font-medium">{beneficiary.name}</TableCell>
                <TableCell>{formatCpf(beneficiary.cpf)}</TableCell>
                <TableCell>{beneficiary.socialProgramCode}</TableCell>
                <TableCell className="text-right">{formatBRL(beneficiary.householdIncome)}</TableCell>
                <TableCell>
                  <BeneficiaryStatusBadge status={beneficiary.status} />
                </TableCell>
                <TableCell>{formatDateBR(beneficiary.registeredAt)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  );
}
