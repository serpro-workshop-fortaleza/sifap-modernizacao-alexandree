import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { EmptyState } from "@/components/states/empty-state";

describe("EmptyState", () => {
  it("displays the message when rendered", () => {
    render(<EmptyState message="Nada por aqui" />);
    expect(screen.getByText("Nada por aqui")).toBeInTheDocument();
  });
});
