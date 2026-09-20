package CampusLab.ms_campuslab_report.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Tabla de hechos simple usada por el servicio de reporteria para calcular KPIs
 * a partir de los eventos consumidos del topico {@code bookings.events}.
 */
@Entity
@Table(name = "booking_fact")
public class BookingFact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public BookingFact() {
    }

    public BookingFact(Long bookingId, Long resourceId, String status, Instant occurredAt) {
        this.bookingId = bookingId;
        this.resourceId = resourceId;
        this.status = status;
        this.occurredAt = occurredAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
