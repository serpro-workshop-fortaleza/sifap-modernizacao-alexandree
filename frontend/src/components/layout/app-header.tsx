import { ThemeToggle } from "@/components/theme/theme-toggle";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";

export function AppHeader() {
  return (
    <header className="flex h-16 items-center justify-end gap-3 border-b bg-background px-4 md:px-6">
      <ThemeToggle />
      <Avatar>
        <AvatarFallback>SF</AvatarFallback>
      </Avatar>
    </header>
  );
}
