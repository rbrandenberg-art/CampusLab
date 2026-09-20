package CampusLab.ms_campuslab_bff.proxy;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Fachada de {@code /api/report/**} (solo lectura) hacia ms-campuslab-report.
 * Autorizacion (en {@code SecurityConfig}): solo Admin.
 */
@RestController
@RequestMapping("/api/report")
public class ReportProxyController {

    private static final String STRIP_PREFIX = "/api/report";

    private final ProxyService proxyService;
    private final String reportServiceUrl;

    public ReportProxyController(ProxyService proxyService,
            @Value("${campuslab.services.report-url}") String reportServiceUrl) {
        this.proxyService = proxyService;
        this.reportServiceUrl = reportServiceUrl;
    }

    @GetMapping({"", "/**"})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        return proxyService.forward(request, body, reportServiceUrl, STRIP_PREFIX);
    }
}
