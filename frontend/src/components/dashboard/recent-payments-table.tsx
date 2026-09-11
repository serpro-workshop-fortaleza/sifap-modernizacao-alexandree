import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { PaymentStatusBadge } from "@/components/dashboard/payment-status-badge";
import { EmptyState } from "@/components/states/empty-state";
import { formatBRL, formatCpf, formatDateBR } from "@/lib/format";
import type { Payment } from "@/types/payment";

export function RecentPaymentsTable({ payments }: { payments: Payment[] }) {
  if (payments.length === 0) {
    return <EmptyState message="Ainda não há pagamentos registrados para esta competência" />;
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Beneficiário</TableHead>
          <TableHead>CPF</TableHead>
          <TableHead className="text-right">Valor líquido</TableHead>
          <TableHead>Situação</TableHead>
          <TableHead>Emitido em</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {payments.map((payment) => (
          <TableRow key={payment.id}>
            <TableCell className="font-medium">{payment.beneficiaryName}</TableCell>
            <TableCell>{formatCpf(payment.beneficiaryCpf)}</TableCell>
            <TableCell className="text-right">{formatBRL(payment.netAmount)}</TableCell>
            <TableCell>
              <PaymentStatusBadge status={payment.status} />
            </TableCell>
            <TableCell>{formatDateBR(payment.issuedAt)}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
