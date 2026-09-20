package CampusLab.ms_campuslab_catalog.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload esperado dentro de los eventos publicados en el tópico
 * {@code bookings.events} por ms-campuslab-bookings. Solo se leen los
 * campos que interesan a Catálogo; el resto del payload se ignora.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingEventPayload(
        Long bookingId,
        Long resourceId
) {
}
