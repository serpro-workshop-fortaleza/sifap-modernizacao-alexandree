"use client";

import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { formatBRL, formatCompetence } from "@/lib/format";
import type { PayrollRunHistoryPoint } from "@/types/payroll";

export function PayrollHistoryChart({ data }: { data: PayrollRunHistoryPoint[] }) {
  const chartData = data.map((point) => ({
    ...point,
    label: formatCompetence(point.competence),
  }));

  return (
    <Card className="col-span-full lg:col-span-2">
      <CardHeader>
        <CardTitle>Valor líquido pago por competência</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="h-72 w-full" role="img" aria-label="Gráfico de valor líquido pago por competência">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
              <XAxis dataKey="label" tick={{ fontSize: 12 }} />
              <YAxis
                tick={{ fontSize: 12 }}
                tickFormatter={(value: number) => formatBRL(value)}
                width={90}
              />
              <Tooltip
                formatter={(value) => formatBRL(Number(value ?? 0))}
                labelClassName="text-foreground"
              />
              <Bar dataKey="totalNetAmount" name="Valor líquido" fill="var(--color-chart-1, #2563eb)" radius={4} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
