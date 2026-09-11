import { AlertTriangle } from "lucide-react";

export function ErrorState({ message }: { message: string }) {
  return (
    <div
      role="alert"
      className="flex flex-col items-center justify-center gap-2 rounded-lg border border-dashed border-destructive/50 bg-destructive/5 p-10 text-center"
    >
      <AlertTriangle className="h-8 w-8 text-destructive" aria-hidden="true" />
      <p className="font-medium text-destructive">Não foi possível carregar os dados</p>
      <p className="text-sm text-muted-foreground">{message}</p>
      <p className="text-xs text-muted-foreground">
        Verifique se o serviço de backend está em execução e acessível.
      </p>
    </div>
  );
}
