package CampusLab.ms_campuslab_report.service;

import java.time.Duration;
import java.time.Instant;

/**
 * Rangos de tiempo soportados por los endpoints de reporteria.
 */
public enum ReportRange {

    LAST_24H("last24h", Duration.ofHours(24)),
    LAST_7D("last7d", Duration.ofDays(7));

    private final String param;
    private final Duration duration;

    ReportRange(String param, Duration duration) {
        this.param = param;
        this.duration = duration;
    }

    public Instant from(Instant now) {
        return now.minus(duration);
    }

    public static ReportRange fromParam(String param) {
        for (ReportRange range : values()) {
            if (range.param.equalsIgnoreCase(param)) {
                return range;
            }
        }
        throw new IllegalArgumentException(
                "range invalido: '" + param + "'. Valores soportados: last24h, last7d");
    }
}
