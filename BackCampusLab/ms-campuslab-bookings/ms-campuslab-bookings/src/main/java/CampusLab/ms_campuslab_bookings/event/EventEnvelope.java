package CampusLab.ms_campuslab_bookings.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope común de eventos usado por todos los microservicios de CampusLab
 * (sección 4 del brief compartido). Cada servicio declara su propia copia
 * mínima: aún no existe una librería compartida entre microservicios.
 *
 * <pre>
 * {
 *   "type": "string (ej. booking.aprobada)",
 *   "eventId": "UUID",
 *   "timestamp": "ISO-8601",
 *   "traceId": "UUID",
 *   "correlationId": "UUID",
 *   "payload": { }
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EventEnvelope<T>(
        String type,
        UUID eventId,
        Instant timestamp,
        UUID traceId,
        UUID correlationId,
        T payload
) {

    /**
     * Crea un envelope nuevo con eventId/traceId propios, generando también
     * un correlationId nuevo. Usar {@link #of(String, Object, UUID)} cuando
     * varias publicaciones (Kafka + RabbitMQ) deben compartir un mismo
     * correlationId por originarse en la misma transición de estado.
     */
    public static <T> EventEnvelope<T> of(String type, T payload) {
        return of(type, payload, UUID.randomUUID());
    }

    public static <T> EventEnvelope<T> of(String type, T payload, UUID correlationId) {
        return new EventEnvelope<>(type, UUID.randomUUID(), Instant.now(), UUID.randomUUID(), correlationId, payload);
    }
}
