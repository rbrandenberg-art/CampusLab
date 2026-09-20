package CampusLab.ms_campuslab_catalog.event;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Envelope común de eventos (sección 4 del brief), copia mínima local del
 * servicio (aún no existe librería compartida entre microservicios).
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
}
