# SIFAP 2.0 — Frontend

Painel Next.js 15 (App Router) + TypeScript + Tailwind CSS + shadcn/ui para o SIFAP modernizado. Consome a API REST do backend Spring Boot em `/api/v1/*`.

## Desenvolvimento local

```bash
npm install
cp .env.local.example .env.local   # ajuste API_URL se o backend não estiver em localhost:8080
npm run dev
```

Abra [http://localhost:3000](http://localhost:3000). A rota inicial redireciona para `/dashboard`.

## Scripts

| Comando | Finalidade |
|---|---|
| `npm run dev` | Servidor de desenvolvimento |
| `npm run build` | Build de produção (`output: standalone`) |
| `npm run lint` | ESLint |
| `npm run test` | Testes Vitest + Testing Library |

## Rotas

| Rota | Descrição |
|---|---|
| `/dashboard` | Indicadores e histórico da folha de pagamento |
| `/beneficiarios` | Lista e filtro de beneficiários |
| `/pagamentos` | Pagamentos gerados pela folha |
| `/folha` | Executa a folha da competência (server action) |
| `/programas` | Catálogo de programas sociais |

## Docker

A imagem de produção usa build multi-stage com `output: standalone` (ver [`Dockerfile`](Dockerfile)). Suba a stack completa (banco + backend + frontend) a partir da raiz do repositório:

```bash
docker compose up --build
```

O frontend fica em `http://localhost:3000` e chama o backend pelo nome de serviço `backend` dentro da rede do Compose (variável `API_URL`).


## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
