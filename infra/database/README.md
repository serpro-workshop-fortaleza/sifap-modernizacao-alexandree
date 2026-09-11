# Infraestrutura local do banco

O ambiente inicial do Estagio 3 e exclusivamente local. O PostgreSQL e
executado pelo servico `db` de [`docker-compose.yml`](../../docker-compose.yml),
com a imagem PostgreSQL 16 fixada por digest.

## Operacao

```bash
docker compose up -d db
docker compose ps
docker compose exec db pg_isready -U sifap -d sifap
docker compose down
```

A aplicacao Spring Boot usa `localhost:5432`, banco `sifap`, usuario `sifap` e
senha somente para desenvolvimento local. Nao ha provisionamento Azure, estado
Terraform ou `terraform apply` nesta fase.

Terraform para Azure sera reintroduzido somente quando houver uma decisao de
implantacao remota, requisitos de ambiente e credenciais provisionadas. Ate la,
o Compose e a definicao operacional da infraestrutura local e a CI valida sua
configuracao e a saude do PostgreSQL.
