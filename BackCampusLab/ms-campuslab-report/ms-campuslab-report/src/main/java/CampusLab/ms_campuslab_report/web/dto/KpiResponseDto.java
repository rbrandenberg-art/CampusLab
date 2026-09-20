package CampusLab.ms_campuslab_report.web.dto;

import java.util.List;

/**
 * Respuesta del endpoint {@code GET /api/report/kpis}.
 *
 * @param reservasPorHora            cantidad de reservas (booking.solicitada) creadas por hora en el rango
 * @param tiempoCicloPromedioMinutos promedio en minutos entre booking.solicitada y booking.devuelta,
 *                                   considerando solo bookingId con ambos eventos dentro del rango.
 *                                   {@code null} si no hay datos suficientes.
 * @param equiposOcupados            cantidad de resourceId distintos cuyo ultimo evento conocido es booking.en_uso
 */
public record KpiResponseDto(
        List<HourlyCountDto> reservasPorHora,
        Double tiempoCicloPromedioMinutos,
        long equiposOcupados
) {
}
