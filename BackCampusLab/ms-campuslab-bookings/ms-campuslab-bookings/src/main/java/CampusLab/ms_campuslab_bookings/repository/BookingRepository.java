package CampusLab.ms_campuslab_bookings.repository;

import CampusLab.ms_campuslab_bookings.domain.Booking;
import CampusLab.ms_campuslab_bookings.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Búsqueda para GET /api/bookings?status=&from=&to= con todos los
     * parámetros opcionales. El filtro de fecha se aplica sobre startTime.
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE (:status IS NULL OR b.status = :status)
              AND (:from IS NULL OR b.startTime >= :from)
              AND (:to IS NULL OR b.startTime <= :to)
            ORDER BY b.requestedAt DESC
            """)
    List<Booking> search(@Param("status") BookingStatus status,
                          @Param("from") Instant from,
                          @Param("to") Instant to);
}
