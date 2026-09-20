package CampusLab.ms_campuslab_bookings.dto;

import CampusLab.ms_campuslab_bookings.domain.Booking;

import java.time.Instant;

/**
 * Respuesta pública de una reserva. Nunca se expone la entidad JPA
 * directamente en los controllers.
 */
public record BookingResponse(
        Long id,
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
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
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
