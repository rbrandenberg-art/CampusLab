package CampusLab.ms_campuslab_bookings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Body de {@code POST /api/bookings}. La reserva siempre nace en estado
 * SOLICITADA; el estado no es parte del request.
 */
public record BookingCreateRequest(
        @NotNull(message = "resourceId es obligatorio")
        Long resourceId,

        @NotBlank(message = "requesterId es obligatorio")
        String requesterId,

        Instant startTime,

        Instant endTime,

        String notes
) {
}
