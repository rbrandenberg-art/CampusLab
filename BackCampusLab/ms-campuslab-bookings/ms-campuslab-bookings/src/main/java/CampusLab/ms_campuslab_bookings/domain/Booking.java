package CampusLab.ms_campuslab_bookings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Reserva de un recurso del catálogo (laboratorio/equipo). No hay FK real
 * hacia ms-campuslab-catalog: resourceId es solo una referencia por id,
 * cada microservicio es dueño de su propia base de datos.
 */
@Entity
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "RESOURCE_ID", nullable = false)
    private Long resourceId;

    @Column(name = "REQUESTER_ID", nullable = false, length = 100)
    private String requesterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "REQUESTED_AT", nullable = false)
    private Instant requestedAt;

    @Column(name = "APPROVED_AT")
    private Instant approvedAt;

    @Column(name = "START_TIME")
    private Instant startTime;

    @Column(name = "END_TIME")
    private Instant endTime;

    @Column(name = "RETURNED_AT")
    private Instant returnedAt;

    @Column(name = "NOTES", length = 1000)
    private String notes;

    protected Booking() {
        // JPA
    }

    public Booking(Long resourceId, String requesterId, Instant startTime, Instant endTime, String notes) {
        this.resourceId = resourceId;
        this.requesterId = requesterId;
        this.status = BookingStatus.SOLICITADA;
        this.requestedAt = Instant.now();
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(Instant returnedAt) {
        this.returnedAt = returnedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
