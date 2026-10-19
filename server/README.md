# Runtomo Backend

## Migrations

Install https://github.com/golang-migrate/migrate

We don't do ORMs here.

### Create New Migration

Make sure active dir is `server/`

```bash
migrate create -ext sql -dir migrations -seq <migration_name>
```

### Apply Migration

```bash
migrate -path migrations -database "postgres://<user>:<password>@<host>:<port>/<db_name>" up
```