"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Users,
  Banknote,
  ClipboardList,
  ShieldCheck,
} from "lucide-react";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { href: "/dashboard", label: "Painel", icon: LayoutDashboard },
  { href: "/beneficiarios", label: "Beneficiários", icon: Users },
  { href: "/pagamentos", label: "Pagamentos", icon: Banknote },
  { href: "/folha", label: "Folha mensal", icon: ClipboardList },
  { href: "/programas", label: "Programas sociais", icon: ShieldCheck },
] as const;

export function AppSidebar() {
  const pathname = usePathname();

  return (
    <nav
      aria-label="Navegação principal"
      className="hidden w-64 shrink-0 border-r bg-sidebar text-sidebar-foreground md:flex md:flex-col"
    >
      <div className="flex h-16 items-center gap-2 border-b px-6">
        <ShieldCheck className="h-6 w-6 text-primary" aria-hidden="true" />
        <span className="text-lg font-semibold">SIFAP 2.0</span>
      </div>
      <ul className="flex flex-1 flex-col gap-1 p-3">
        {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
          const active = pathname === href || pathname.startsWith(`${href}/`);
          return (
            <li key={href}>
              <Link
                href={href}
                aria-current={active ? "page" : undefined}
                className={cn(
                  "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  active
                    ? "bg-sidebar-primary text-sidebar-primary-foreground"
                    : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                )}
              >
                <Icon className="h-4 w-4" aria-hidden="true" />
                {label}
              </Link>
            </li>
          );
        })}
      </ul>
      <div className="border-t p-4 text-xs text-sidebar-foreground/60">
        Fiscalização e Administração de Pagamentos
      </div>
    </nav>
  );
}
