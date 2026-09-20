package CampusLab.ms_campuslab_bookings.service;

import CampusLab.ms_campuslab_bookings.domain.Booking;
import CampusLab.ms_campuslab_bookings.domain.BookingStatus;
import CampusLab.ms_campuslab_bookings.dto.BookingCreateRequest;
import CampusLab.ms_campuslab_bookings.dto.BookingResponse;
import CampusLab.ms_campuslab_bookings.event.BookingCommandPublisher;
import CampusLab.ms_campuslab_bookings.event.BookingEventPublisher;
import CampusLab.ms_campuslab_bookings.exception.BookingNotFoundException;
import CampusLab.ms_campuslab_bookings.exception.InvalidBookingStateTransitionException;
import CampusLab.ms_campuslab_bookings.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Máquina de estados de la reserva (sección 3 del brief). La regla clave es
 * que no se puede pasar a EN_USO sin haber pasado por APROBADA; por eso el
 * grafo de transiciones no permite saltos.
 */
@Service
public class BookingService {

    private static final Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = new EnumMap<>(BookingStatus.class);

    static {
        TRANSITIONS.put(BookingStatus.SOLICITADA, EnumSet.of(BookingStatus.APROBADA, BookingStatus.CANCELADA));
        TRANSITIONS.put(BookingStatus.APROBADA, EnumSet.of(BookingStatus.EN_PREPARACION, BookingStatus.CANCELADA));
        TRANSITIONS.put(BookingStatus.EN_PREPARACION, EnumSet.of(BookingStatus.EN_USO));
        TRANSITIONS.put(BookingStatus.EN_USO, EnumSet.of(BookingStatus.DEVUELTA));
        TRANSITIONS.put(BookingStatus.DEVUELTA, EnumSet.noneOf(BookingStatus.class));
        TRANSITIONS.put(BookingStatus.CANCELADA, EnumSet.noneOf(BookingStatus.class));
    }

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;
    private final BookingCommandPublisher commandPublisher;

    public BookingService(BookingRepository bookingRepository,
                           BookingEventPublisher eventPublisher,
                           BookingCommandPublisher commandPublisher) {
        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.commandPublisher = commandPublisher;
    }

    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        Booking booking = new Booking(
                request.resourceId(),
                request.requesterId(),
                request.startTime(),
                request.endTime(),
                request.notes()
        );
        booking = bookingRepository.save(booking);

        eventPublisher.publishStatusChanged(booking, UUID.randomUUID());

        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id) {
        return BookingResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> search(BookingStatus status, Instant from, Instant to) {
        return bookingRepository.search(status, from, to).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse updateStatus(Long id, String rawStatus) {
        BookingStatus newStatus = parseStatus(rawStatus);
        Booking booking = findOrThrow(id);
        BookingStatus current = booking.getStatus();

        Set<BookingStatus> allowed = TRANSITIONS.getOrDefault(current, EnumSet.noneOf(BookingStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new InvalidBookingStateTransitionException(
                    "No se puede pasar de " + current + " a " + newStatus);
        }

        booking.setStatus(newStatus);
        Instant now = Instant.now();
        if (newStatus == BookingStatus.APROBADA) {
            booking.setApprovedAt(now);
        } else if (newStatus == BookingStatus.DEVUELTA) {
            booking.setReturnedAt(now);
        }

        booking = bookingRepository.save(booking);

        // Un solo correlationId para todas las publicaciones que se disparan
        // por esta misma transición (Kafka + comandos RabbitMQ relacionados).
        UUID correlationId = UUID.randomUUID();
        eventPublisher.publishStatusChanged(booking, correlationId);

        if (newStatus == BookingStatus.APROBADA) {
            commandPublisher.publishPrepTicket(booking, correlationId);
            commandPublisher.publishEmailApproved(booking, correlationId);
        } else if (newStatus == BookingStatus.DEVUELTA) {
            commandPublisher.publishEmailReturned(booking, correlationId);
        }

        return BookingResponse.from(booking);
    }

    private Booking findOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Reserva no encontrada: " + id));
    }

    private BookingStatus parseStatus(String raw) {
        try {
            return BookingStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado invalido: " + raw);
        }
    }
}
