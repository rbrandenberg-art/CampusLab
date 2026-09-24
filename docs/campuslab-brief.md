# CampusLab — Brief compartido para agentes de backend/frontend

Este documento resume el enunciado del caso semestral (`Caso 2 - CampusLab.docx`) y las convenciones ya decididas para el proyecto en `D:\DUOC\CloudRachell`. Léelo completo antes de tocar código: fija las reglas que deben ser IDÉNTICAS entre microservicios para que todo interopere.

## 1. Contexto del negocio

Plataforma para que una red de 20 laboratorios de instituciones de educación superior permita:
- Reservar laboratorios y equipos por la web, administrar stock de insumos y coordinar el uso de salas.
- Notificar al estudiante (email/push) y al técnico de laboratorio (ticket de preparación).
- Panel de operaciones en tiempo real (reservas por hora, tiempo de ciclo, equipos ocupados).
- Auditar eventos académicos (quién solicitó, aprobó, entregó o recibió de vuelta un equipo).

## 2. Actores y roles

| Rol | Responsabilidad |
|---|---|
| Admin | Administra el catálogo de labs/equipos y ve KPIs de ocupación |
| Técnico (Operador) | Aprueba reservas, prepara la sala y registra devoluciones |
| Estudiante (Cliente) | Solicita y sigue sus reservas |
| Auditor | Consulta el timeline. Solo lectura |

## 3. Máquina de estados de una reserva (booking)

`SOLICITADA → APROBADA → EN_PREPARACIÓN → EN_USO → DEVUELTA / CANCELADA`

Regla clave: **no se puede pasar a EN_USO sin haber pasado por APROBADA**. El stock/cupo del recurso disminuye al aprobar la reserva (no al solicitar).

## 4. Convenciones técnicas OBLIGATORIAS (para que los microservicios interoperen)

- **Java 21**, **Spring Boot 4.0.8** (ya definido en los `pom.xml` existentes — no cambiar la versión de Spring Boot).
- Paquete base: `CampusLab.ms_campuslab_<nombre>` (guion bajo, snake_case), igual que ya está generado en cada proyecto. Ejemplo: `CampusLab.ms_campuslab_bookings`.
- Grupo Maven: `CampusLab`. Artifact: `ms-campuslab-<nombre>` (con guion).
- **Base de datos de desarrollo: Oracle XE en Docker** (no H2, no Oracle Cloud). Cada microservicio con persistencia (`bookings`, `catalog`, `audit`, `report`) debe:
  - Agregar `spring-boot-starter-data-jpa` + driver `com.oracle.database.jdbc:ojdbc11` (sin versión explícita si el BOM de Spring Boot la resuelve; si no, usar la última estable compatible con Java 21).
  - Definir en `application.yaml` un datasource apuntando a `jdbc:oracle:thin:@//localhost:1521/XEPDB1` con usuario/password parametrizados por variables de entorno `DB_USER` / `DB_PASSWORD` (default de desarrollo: usuario `campuslab_<nombre>`, password `campuslab`).
  - `spring.jpa.hibernate.ddl-auto: update` en desarrollo (no usar `create-drop`).
  - No te preocupes por levantar el contenedor Oracle: eso lo hace un agente de infraestructura aparte con `docker-compose`. Tu trabajo es dejar el código y la config listos para conectarse a esa base cuando exista.
- **Azure AD / JWT: aún no hay App Registration real.** Configura Spring Security así (mismo patrón en todos los servicios):
  ```yaml
  spring:
    security:
      oauth2:
        resourceserver:
          jwt:
            https://login.microsoftonline.com/${AZURE_TENANT_ID:CHANGEME-TENANT-ID}/v2.0
  ```
  Y en el `SecurityFilterChain`, exigir JWT válido en todos los endpoints salvo `/actuator/health`, con autorización por rol usando el claim de roles del token (`roles` o `scp` según venga de Azure AD — dejarlo configurable con un converter, ya que el nombre exacto del claim se ajustará cuando exista el App Registration real). Usa variables de entorno para `AZURE_TENANT_ID` y `AZURE_CLIENT_ID` con un valor por defecto de placeholder (`CHANGEME-TENANT-ID`, `CHANGEME-CLIENT-ID`) para que el proyecto compile y levante igual sin credenciales reales.
- **Envelope común de eventos** (RabbitMQ y Kafka) — todos los mensajes que publiques o consumas deben tener esta forma (JSON):
  ```json
  {
    "type": "string (ej. booking.approved)",
    "eventId": "UUID",
    "timestamp": "ISO-8601",
    "traceId": "UUID",
    "correlationId": "UUID",
    "payload": { }
  }
  ```
  Define una clase `EventEnvelope<T>` (o similar) reutilizable dentro de cada servicio (aún no hay librería compartida entre microservicios — cada uno declara su propia copia mínima).
- Cada servicio expone `/actuator/health` (agrega `spring-boot-starter-actuator` si hace falta).
- Al terminar, compila con el wrapper del propio proyecto: `./mvnw.cmd -q -DskipTests package` (Windows) y deja constancia de si compiló o no. No necesitas que la base de datos ni Azure AD reales existan para que compile.

## 5. Microservicios de dominio (tabla completa del enunciado)

| Servicio | Dominio | DB | Responsabilidad | Exposición |
|---|---|---|---|---|
| ms-campuslab-bookings | Reservas | Oracle | CRUD reservas, estados, coordinación de stock y notificación | `/api/bookings/*` |
| ms-campuslab-catalog | Labs / equipos / insumos | Oracle | CRUD recursos, stock y cupos | `/api/catalog/*` |
| ms-campuslab-notify | Notificaciones | sin DB | Procesa envío email/webpush y ticket de prep. vía RabbitMQ | no público (consumidor RabbitMQ) |
| ms-campuslab-audit | Auditoría / timeline | Oracle | Consume Kafka y persiste eventos | `/api/audit/*` (read-only) |
| ms-campuslab-report | KPIs / analytics | Oracle | Agregaciones y endpoints de lectura (consume Kafka) | `/api/report/*` (read-only) |
| ms-campuslab-bff | — | — | Fachada Spring Boot + Spring Security detrás del API Gateway | enruta a los anteriores |
| ms-campuslab-rabbitmq | — | — | Administrador de topología RabbitMQ (colas/exchanges/DLQ) | admin, no público |
| ms-campuslab-kafka | — | — | Administrador de topología Kafka (tópicos/particiones) | admin, no público |

Flujo de llamadas seguro: **JWT → API Gateway → ms-campuslab-bff → microservicio de dominio.**

## 6. Endpoints esenciales exigidos por el enunciado

**ms-campuslab-bookings**
- `POST /api/bookings` (crear reserva)
- `GET /api/bookings/{id}`
- `PUT /api/bookings/{id}/status` body: `{ "status": "SOLICITADA|APROBADA|EN_PREPARACIÓN|EN_USO|DEVUELTA|CANCELADA" }`
- `GET /api/bookings?status=...&from=...&to=...`

**ms-campuslab-catalog**
- `GET /api/catalog/resources`
- `POST /api/catalog/resources`
- `PUT /api/catalog/resources/{id}` (cupo/stock)

**ms-campuslab-report**
- `GET /api/report/kpis?range=last24h`
- `GET /api/report/top-resources?range=last7d`

(audit y notify no tienen endpoints explícitos en el enunciado más allá de lo ya descrito arriba — audit expone lectura de timeline, notify no expone API pública).

## 7. Topología RabbitMQ (6 colas: 3 flujos + 3 DLQ)

Exchanges: `cmd.direct` (direct), `cmd.topic` (topic), `cmd.dead.dlx` (direct, para DLQ).

| Cola principal | Propósito | DLQ | Binding direct | Binding topic |
|---|---|---|---|---|
| q.cmd.email | Email/push al estudiante (aprobación, sala lista, devolución) | q.cmd.email.dlq | email.send | email.* |
| q.cmd.prep | Ticket de preparación de sala/equipo al técnico | q.cmd.prep.dlq | prep.ticket | prep.# |
| q.cmd.voucher | Generación de PDF (vale de retiro o acta de devolución) | q.cmd.voucher.dlq | voucher.gen | voucher.* |

Buenas prácticas: envelope común (ver sección 4), ACK/NACK explícitos, idempotencia y métricas de tasa de DLQ.

## 8. Topología Kafka

| Tópico | Particiones | Réplicas | Política | Retención | Propósito |
|---|---|---|---|---|---|
| bookings.events | 3 | 3 | delete | 3–7 días | Fuente de verdad de eventos de la reserva. Alimenta reportería y auditoría |
| audit.timeline | 3 | 3 | compact,delete | 14–30 días | Historial quién/qué/cuándo/desde dónde |
| *.DLT (por consumidor) | 3 | 3 | delete | 7–14 días | Mensajes que fallaron tras N reintentos, con metadatos de error |

## 9. Pantallas del frontend (React + MSAL — Angular ya NO es requisito, decisión del usuario 2026-09-17)

| Pantalla | Ruta | Roles | Función |
|---|---|---|---|
| Login | `/login` | público | MSAL. Botón «Iniciar sesión con Microsoft» |
| Dashboard | `/dashboard` | todos los autenticados | Admin: ocupación de labs. Técnico: reservas por preparar. Estudiante: próximas reservas y estado |
| Reservas | `/bookings` | Admin, Técnico, Estudiante | Listar, crear (estudiante o técnico) y cambiar estado (técnico/admin) |
| Catálogo de recursos | `/catalog` | Admin, Técnico | Laboratorios, equipos e insumos |
| Reportería | `/reports` | Admin | Reservas por hora, tiempo de ciclo, recursos más usados |
| Auditoría | `/audit` | Admin, Auditor | Trazabilidad de la reserva. Filtros: usuario, fechas, tipo de evento |

## 10. Estado actual del proyecto (antes de tu tarea)

Los 8 microservicios en `BackCampusLab/` son scaffolds de Spring Initializr: solo tienen la clase `*Application.java` y un `application.yaml` con `spring.application.name`. El frontend en `CampuLab/` (React + Vite + `@azure/msal-react`) solo tiene login funcional (`App.tsx` + `ProtectedData.tsx`), sin router ni pantallas de negocio.
