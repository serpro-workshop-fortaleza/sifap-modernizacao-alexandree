import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { PaymentStatusBadge } from "@/components/dashboard/payment-status-badge";

describe("PaymentStatusBadge", () => {
  it("displays the Portuguese label when status is PAGO", () => {
    render(<PaymentStatusBadge status="PAGO" />);
    expect(screen.getByText("Pago")).toBeInTheDocument();
  });

  it("displays the Portuguese label when status is REJEITADO", () => {
    render(<PaymentStatusBadge status="REJEITADO" />);
    expect(screen.getByText("Rejeitado")).toBeInTheDocument();
  });
});
