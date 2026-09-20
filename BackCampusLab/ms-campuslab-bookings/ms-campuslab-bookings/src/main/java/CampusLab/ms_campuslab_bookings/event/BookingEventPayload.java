package CampusLab.ms_campuslab_bookings.event;

import CampusLab.ms_campuslab_bookings.domain.Booking;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

/**
 * Payload de los eventos/comandos publicados por este servicio. Los nombres
 * de campo ("bookingId", "resourceId", "status", ...) son un contrato ya
 * consumido por ms-campuslab-catalog, ms-campuslab-audit, ms-campuslab-report
 * y ms-campuslab-notify: no renombrar sin coordinar con esos servicios.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BookingEventPayload(
        Long bookingId,
        Long resourceId,
        String requesterId,
        String status,
        Instant requestedAt,
        Instant approvedAt,
        Instant startTime,
        Instant endTime,
        Instant returnedAt,
        String notes
) {
    public static BookingEventPayload from(Booking booking) {
        return new BookingEventPayload(
                booking.getId(),
                booking.getResourceId(),
                booking.getRequesterId(),
                booking.getStatus().name(),
                booking.getRequestedAt(),
                booking.getApprovedAt(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getReturnedAt(),
                booking.getNotes()
        );
    }
}
