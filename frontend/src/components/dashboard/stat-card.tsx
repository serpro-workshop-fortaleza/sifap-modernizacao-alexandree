import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

interface StatCardProps {
  title: string;
  value: string;
  icon: LucideIcon;
  trend?: string;
  trendPositive?: boolean;
}

export function StatCard({ title, value, icon: Icon, trend, trendPositive }: StatCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" aria-hidden="true" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold">{value}</div>
        {trend && (
          <p
            className={cn(
              "mt-1 text-xs",
              trendPositive ? "text-emerald-600 dark:text-emerald-400" : "text-muted-foreground",
            )}
          >
            {trend}
          </p>
        )}
      </CardContent>
    </Card>
  );
}
