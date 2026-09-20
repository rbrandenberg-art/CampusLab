package CampusLab.ms_campuslab_bff.proxy;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Fachada de {@code /api/catalog/**} hacia ms-campuslab-catalog.
 * Autorizacion (en {@code SecurityConfig}): GET -> Admin/Tecnico; POST/PUT/PATCH/DELETE -> solo Admin.
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogProxyController {

    private static final String STRIP_PREFIX = "/api/catalog";

    private final ProxyService proxyService;
    private final String catalogServiceUrl;

    public CatalogProxyController(ProxyService proxyService,
            @Value("${campuslab.services.catalog-url}") String catalogServiceUrl) {
        this.proxyService = proxyService;
        this.catalogServiceUrl = catalogServiceUrl;
    }

    @RequestMapping(value = {"", "/**"},
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        return proxyService.forward(request, body, catalogServiceUrl, STRIP_PREFIX);
    }
}
