package CampusLab.ms_campuslab_bff.proxy;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Fachada de {@code /api/audit/**} (solo lectura) hacia ms-campuslab-audit.
 * Autorizacion (en {@code SecurityConfig}): Admin y Auditor.
 */
@RestController
@RequestMapping("/api/audit")
public class AuditProxyController {

    private static final String STRIP_PREFIX = "/api/audit";

    private final ProxyService proxyService;
    private final String auditServiceUrl;

    public AuditProxyController(ProxyService proxyService,
            @Value("${campuslab.services.audit-url}") String auditServiceUrl) {
        this.proxyService = proxyService;
        this.auditServiceUrl = auditServiceUrl;
    }

    @GetMapping({"", "/**"})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        return proxyService.forward(request, body, auditServiceUrl, STRIP_PREFIX);
    }
}
