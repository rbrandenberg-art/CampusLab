package CampusLab.ms_campuslab_bff.proxy;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Fachada de {@code /api/bookings/**} hacia ms-campuslab-bookings.
 * La autorizacion por rol (Admin, Tecnico, Estudiante) se aplica en {@code SecurityConfig},
 * antes de que la request llegue aqui.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingsProxyController {

    private static final String STRIP_PREFIX = "";

    private final ProxyService proxyService;
    private final String bookingsServiceUrl;

    public BookingsProxyController(ProxyService proxyService,
            @Value("${campuslab.services.bookings-url}") String bookingsServiceUrl) {
        this.proxyService = proxyService;
        this.bookingsServiceUrl = bookingsServiceUrl;
    }

    @RequestMapping(value = {"", "/**"},
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        return proxyService.forward(request, body, bookingsServiceUrl, STRIP_PREFIX);
    }
}
