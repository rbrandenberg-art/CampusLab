package CampusLab.ms_campuslab_report.service;

import CampusLab.ms_campuslab_report.domain.BookingFact;
import CampusLab.ms_campuslab_report.repository.BookingFactRepository;
import CampusLab.ms_campuslab_report.web.dto.HourlyCountDto;
import CampusLab.ms_campuslab_report.web.dto.KpiResponseDto;
import CampusLab.ms_campuslab_report.web.dto.TopResourceDto;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Calcula los KPIs de reporteria a partir de los {@link BookingFact} proyectados
 * desde el topico {@code bookings.events}.
 *
 * Se prioriza claridad sobre optimizacion: las agregaciones mas simples se resuelven
 * con derived queries de Spring Data y el resto en memoria (volumen academico).
 */
@Service
public class ReportService {

    private static final String STATUS_SOLICITADA = "SOLICITADA";
    private static final String STATUS_DEVUELTA = "DEVUELTA";
    private static final String STATUS_EN_USO = "EN_USO";

    private final BookingFactRepository bookingFactRepository;

    public ReportService(BookingFactRepository bookingFactRepository) {
        this.bookingFactRepository = bookingFactRepository;
    }

    public KpiResponseDto getKpis(ReportRange range) {
        Instant now = Instant.now();
        Instant from = range.from(now);

        List<BookingFact> factsInRange = bookingFactRepository.findByOccurredAtBetween(from, now);

        List<HourlyCountDto> reservasPorHora = reservasPorHora(factsInRange);
        Double tiempoCicloPromedioMinutos = tiempoCicloPromedioMinutos(factsInRange);
        long equiposOcupados = equiposOcupados();

        return new KpiResponseDto(reservasPorHora, tiempoCicloPromedioMinutos, equiposOcupados);
    }

    public List<TopResourceDto> getTopResources(ReportRange range) {
        Instant now = Instant.now();
        Instant from = range.from(now);

        List<BookingFact> solicitadas = bookingFactRepository
                .findByStatusAndOccurredAtBetween(STATUS_SOLICITADA, from, now);

        Map<Long, Long> countByResource = solicitadas.stream()
                .filter(fact -> fact.getResourceId() != null)
                .collect(Collectors.groupingBy(BookingFact::getResourceId, Collectors.counting()));

        return countByResource.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new TopResourceDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<HourlyCountDto> reservasPorHora(List<BookingFact> factsInRange) {
        Map<Instant, Long> countByHour = factsInRange.stream()
                .filter(fact -> STATUS_SOLICITADA.equals(fact.getStatus()))
                .collect(Collectors.groupingBy(
                        fact -> fact.getOccurredAt().truncatedTo(ChronoUnit.HOURS),
                        TreeMap::new,
                        Collectors.counting()));

        return countByHour.entrySet().stream()
                .map(entry -> new HourlyCountDto(entry.getKey().toString(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Double tiempoCicloPromedioMinutos(List<BookingFact> factsInRange) {
        Map<Long, List<BookingFact>> byBooking = factsInRange.stream()
                .filter(fact -> fact.getBookingId() != null)
                .collect(Collectors.groupingBy(BookingFact::getBookingId));

        List<Double> ciclosEnMinutos = byBooking.values().stream()
                .map(this::cicloEnMinutos)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (ciclosEnMinutos.isEmpty()) {
            return null;
        }
        double promedio = ciclosEnMinutos.stream().mapToDouble(Double::doubleValue).average().orElse(0d);
        return Math.round(promedio * 100.0) / 100.0;
    }

    private Double cicloEnMinutos(List<BookingFact> factsDeUnaReserva) {
        Instant primeraSolicitud = factsDeUnaReserva.stream()
                .filter(fact -> STATUS_SOLICITADA.equals(fact.getStatus()))
                .map(BookingFact::getOccurredAt)
                .min(Comparator.naturalOrder())
                .orElse(null);

        Instant devolucion = factsDeUnaReserva.stream()
                .filter(fact -> STATUS_DEVUELTA.equals(fact.getStatus()))
                .map(BookingFact::getOccurredAt)
                .min(Comparator.naturalOrder())
                .orElse(null);

        if (primeraSolicitud == null || devolucion == null) {
            return null;
        }
        return Duration.between(primeraSolicitud, devolucion).toSeconds() / 60.0;
    }

    private long equiposOcupados() {
        Map<Long, BookingFact> ultimoEventoPorRecurso = bookingFactRepository.findAll().stream()
                .filter(fact -> fact.getResourceId() != null)
                .collect(Collectors.toMap(
                        BookingFact::getResourceId,
                        fact -> fact,
                        (a, b) -> a.getOccurredAt().isAfter(b.getOccurredAt()) ? a : b));

        return ultimoEventoPorRecurso.values().stream()
                .filter(fact -> STATUS_EN_USO.equals(fact.getStatus()))
                .count();
    }
}
