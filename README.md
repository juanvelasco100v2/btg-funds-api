# BTG Funds API

API REST para la gestion de fondos de inversion BTG Pactual.

## Stack

- Java 25 + Spring Boot 4.0.3
- Gradle 9.3.1
- AWS DynamoDB (Enhanced Client)
- AWS SES + SNS (notificaciones)
- JWT (jjwt) + Spring Security
- Swagger/OpenAPI (springdoc)
- Docker + ECS Fargate

## Ejecucion local

```bash
gradle bootRun
```

Requiere credenciales AWS configuradas (`aws configure`) y las tablas DynamoDB creadas por el stack de infraestructura.

Variables de entorno opcionales (tienen defaults para dev):

| Variable | Default | Descripcion |
|---|---|---|
| `SERVER_PORT` | 8080 | Puerto del servidor |
| `AWS_REGION` | us-east-1 | Region AWS |
| `DYNAMODB_TABLE_USERS` | dev-users | Tabla de usuarios |
| `DYNAMODB_TABLE_FUNDS` | dev-funds | Tabla de fondos |
| `DYNAMODB_TABLE_TRANSACTIONS` | dev-transactions | Tabla de transacciones |
| `DYNAMODB_TABLE_ROLES` | dev-roles | Tabla de roles |
| `DYNAMODB_TABLE_SUBSCRIPTIONS` | dev-subscriptions | Tabla de suscripciones |
| `SES_SENDER_EMAIL` | noreply@juanvelasco100.click | Email remitente SES |
| `JWT_SECRET` | (default dev key) | Clave secreta JWT |

## Endpoints

| Metodo | Path | Descripcion | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Registrar usuario | No |
| POST | `/api/auth/login` | Iniciar sesion | No |
| POST | `/api/setup/admin` | Crear admin inicial (solo funciona una vez) | No |
| GET | `/api/funds` | Listar fondos | Si |
| GET | `/api/funds/{id}` | Detalle de fondo | Si |
| POST | `/api/subscriptions/{fundId}` | Suscribirse a fondo | Si |
| DELETE | `/api/subscriptions/{fundId}` | Cancelar suscripcion | Si |
| GET | `/api/subscriptions` | Mis suscripciones | Si |
| GET | `/api/transactions` | Historial (paginado) | Si |
| GET | `/api/users/me` | Mi perfil | Si |
| PUT | `/api/users/me` | Actualizar perfil | Si |
| GET | `/api/admin/users` | Listar usuarios | ADMIN |
| POST | `/api/admin/roles` | Crear rol | ADMIN |
| PUT | `/api/admin/roles/{id}` | Actualizar rol | ADMIN |
| DELETE | `/api/admin/roles/{id}` | Eliminar rol | ADMIN |
| GET | `/api/admin/roles` | Listar roles | ADMIN |
| GET | `/api/admin/roles/{id}` | Detalle de rol | ADMIN |

Swagger UI: `/swagger-ui.html`

## Operaciones transaccionales

Las operaciones de suscripcion (`subscribe`) y cancelacion (`cancel`) son transaccionales usando `DynamoDB TransactWriteItems`. Esto garantiza que las 3 operaciones se ejecuten de forma atomica (todas o ninguna):

- Actualizar balance del usuario
- Crear/eliminar la suscripcion
- Registrar la transaccion

## Setup inicial

1. Desplegar infraestructura (proyecto `nuevo/`)
2. Ejecutar seed de roles: `bash scripts/roles.sh dev-roles us-east-1` (en proyecto infra)
3. Hacer push a `dev` para que CI/CD despliegue la app
4. Crear admin inicial:

```bash
curl -X POST https://juanvelasco100.click/api/setup/admin \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@btgpactual.com","name":"Admin BTG","password":"Admin2024!","phone":"+573001234567"}'
```

## CI/CD (GitHub Actions)

El workflow se activa al hacer push a `main` o `dev`. Ejecuta:

1. **Build & Test** - Compila y ejecuta tests (en PRs a main y dev tambien)
2. **Deploy** (solo en push a main/dev):
   - Build de imagen Docker
   - Push a ECR (tag SHA + latest)
   - Registra nueva revision del task definition con la imagen nueva
   - Actualiza el servicio ECS con `desired-count 1`

### Secrets requeridos (GitHub)

| Secret | Valor                                              | Descripcion |
|---|----------------------------------------------------|---|
| `AWS_ACCESS_KEY_ID` | (tu access key)                                    | Credenciales AWS |
| `AWS_SECRET_ACCESS_KEY` | (tu secret key)                                    | Credenciales AWS |
| `ECR_REPOSITORY_URI` | `cuentaId.dkr.ecr.us-east-1.amazonaws.com/dev-api` | URI del repositorio ECR (fijo) |
| `ECS_CLUSTER_NAME` | `dev-cluster`                                      | Nombre del cluster ECS |
| `ECS_SERVICE_NAME` | `dev-api-service`                                  | Nombre del servicio ECS |
| `ECS_TASK_FAMILY` | `dev-api`                                          | Family del task definition |

### Flujo destroy + recreate

Si destruyes y recreas la infraestructura, solo necesitas hacer push a `dev` y el CI/CD reconstruye y despliega todo automaticamente. Los secrets no cambian.

## Arquitectura

```
Cliente -> ALB (HTTPS) -> ECS Fargate (auto scaling 1-2) -> DynamoDB
                                                          -> SES (email)
                                                          -> SNS (SMS)
```

La autorizacion es dinamica: los permisos de cada rol se almacenan en DynamoDB y se evaluan en tiempo de ejecucion usando `AntPathMatcher`, sin necesidad de cambiar codigo para agregar nuevos roles o permisos.
