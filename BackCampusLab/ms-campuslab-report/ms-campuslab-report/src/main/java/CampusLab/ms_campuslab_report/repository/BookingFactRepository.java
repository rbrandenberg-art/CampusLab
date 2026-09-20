package CampusLab.ms_campuslab_report.repository;

import CampusLab.ms_campuslab_report.domain.BookingFact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface BookingFactRepository extends JpaRepository<BookingFact, Long> {

    List<BookingFact> findByOccurredAtBetween(Instant from, Instant to);

    List<BookingFact> findByStatusAndOccurredAtBetween(String status, Instant from, Instant to);
}
