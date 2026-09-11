import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { StatCard } from "@/components/dashboard/stat-card";
import { Banknote } from "lucide-react";

describe("StatCard", () => {
  it("displays the title and value when provided", () => {
    render(<StatCard title="Valor líquido" value="R$ 1.000,00" icon={Banknote} />);
    expect(screen.getByText("Valor líquido")).toBeInTheDocument();
    expect(screen.getByText("R$ 1.000,00")).toBeInTheDocument();
  });

  it("displays the trend text when a trend is provided", () => {
    render(<StatCard title="Rejeições" value="0" icon={Banknote} trend="Nenhuma rejeição" trendPositive />);
    expect(screen.getByText("Nenhuma rejeição")).toBeInTheDocument();
  });
});
