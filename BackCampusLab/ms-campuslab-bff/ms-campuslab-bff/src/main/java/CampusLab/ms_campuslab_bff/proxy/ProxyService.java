package CampusLab.ms_campuslab_bff.proxy;

import java.net.URI;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Reenvia (proxy) una request HTTP entrante del BFF hacia un microservicio de dominio,
 * propagando el header {@code Authorization} (JWT) tal cual, junto con el resto de headers,
 * metodo, query string y cuerpo. La autorizacion por rol ya fue resuelta antes de llegar aqui
 * por {@link CampusLab.ms_campuslab_bff.security.SecurityConfig}.
 */
@Service
public class ProxyService {

    /** Headers de salto unico (hop-by-hop) que no deben reenviarse tal cual. */
    private static final Set<String> SKIP_REQUEST_HEADERS = Set.of(
            "host", "content-length", "connection", "keep-alive",
            "transfer-encoding", "upgrade", "te", "trailer",
            "proxy-authenticate", "proxy-authorization", "expect");

    private static final Set<String> SKIP_RESPONSE_HEADERS = Set.of(
            "connection", "keep-alive", "transfer-encoding", "upgrade", "trailer", "content-length");

    private final RestClient restClient;

    public ProxyService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    /**
     * @param request     request HTTP original recibida por el BFF.
     * @param body        cuerpo crudo ya leido de la request (puede ser vacio).
     * @param targetBaseUrl base URL del microservicio de dominio destino (ej. http://localhost:8081).
     * @param stripPrefix prefijo del path del BFF que se reemplaza por la base del destino
     *                    (ej. "/api/bookings").
     */
    public ResponseEntity<byte[]> forward(HttpServletRequest request, byte[] body, String targetBaseUrl,
            String stripPrefix) {
        URI targetUri = buildTargetUri(request, targetBaseUrl, stripPrefix);

        RestClient.RequestBodySpec spec = restClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(targetUri);

        copyRequestHeaders(request, spec);

        try {
            if (body != null && body.length > 0) {
    spec.body(body);
}

            return spec.exchange((clientRequest, clientResponse) -> {
                byte[] responseBody = clientResponse.getBody().readAllBytes();
                HttpHeaders headers = filterResponseHeaders(clientResponse.getHeaders());
                return ResponseEntity.status(clientResponse.getStatusCode())
                        .headers(headers)
                        .body(responseBody);
            }, false);
        } catch (ResourceAccessException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("{\"error\":\"No fue posible contactar el servicio de dominio en "
                            + targetBaseUrl + "\"}").getBytes());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("{\"error\":\"Fallo al reenviar la solicitud: " + ex.getMessage() + "\"}").getBytes());
        }
    }

    private URI buildTargetUri(HttpServletRequest request, String targetBaseUrl, String stripPrefix) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        String remaining = path.startsWith(stripPrefix) ? path.substring(stripPrefix.length()) : path;

        String base = targetBaseUrl.endsWith("/") ? targetBaseUrl.substring(0, targetBaseUrl.length() - 1) : targetBaseUrl;
        String query = request.getQueryString();

        String url = base + remaining + (query != null && !query.isBlank() ? "?" + query : "");
        return URI.create(url);
    }

    private void copyRequestHeaders(HttpServletRequest request, RestClient.RequestBodySpec spec) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return;
        }
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (SKIP_REQUEST_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                continue;
            }
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                spec.header(name, values.nextElement());
            }
        }
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders source) {
        HttpHeaders headers = new HttpHeaders();
        source.forEach((name, values) -> {
            if (!SKIP_RESPONSE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                headers.put(name, values);
            }
        });
        return headers;
    }
}
