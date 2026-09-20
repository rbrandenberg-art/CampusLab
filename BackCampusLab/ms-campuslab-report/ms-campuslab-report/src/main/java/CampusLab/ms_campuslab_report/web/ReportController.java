package CampusLab.ms_campuslab_report.web;

import CampusLab.ms_campuslab_report.service.ReportRange;
import CampusLab.ms_campuslab_report.service.ReportService;
import CampusLab.ms_campuslab_report.web.dto.KpiResponseDto;
import CampusLab.ms_campuslab_report.web.dto.TopResourceDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de solo lectura para el panel de operaciones (reservas por hora, tiempo de
 * ciclo, equipos ocupados, recursos mas usados). Ver seccion 6 del brief de CampusLab.
 */
@RestController
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/api/report/kpis")
    public KpiResponseDto getKpis(@RequestParam(defaultValue = "last24h") String range) {
        return reportService.getKpis(ReportRange.fromParam(range));
    }

    @GetMapping("/api/report/top-resources")
    public List<TopResourceDto> getTopResources(@RequestParam(defaultValue = "last7d") String range) {
        return reportService.getTopResources(ReportRange.fromParam(range));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRange(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
