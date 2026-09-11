/**
 * Base da API do backend, resolvida no servidor (nome do serviço Docker) e
 * exposta ao cliente somente quando prefixada com NEXT_PUBLIC_.
 */
export function getServerApiUrl(): string {
  return process.env.API_URL ?? "http://backend:8080";
}
