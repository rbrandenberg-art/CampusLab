package CampusLab.ms_campuslab_report.web.dto;

/**
 * Cantidad de reservas creadas (booking.solicitada) en una hora del rango consultado.
 *
 * @param hour  hora en formato ISO truncada (ej. 2026-09-17T14:00:00Z)
 * @param count cantidad de reservas creadas en esa hora
 */
public record HourlyCountDto(String hour, long count) {
}
