package CampusLab.ms_campuslab_bookings.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Body de {@code PUT /api/bookings/{id}/status}: {"status": "APROBADA"}.
 */
public record BookingStatusUpdateRequest(
        @NotBlank(message = "status es obligatorio")
        String status
) {
}
