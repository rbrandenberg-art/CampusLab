package CampusLab.ms_campuslab_bookings.controller;

import CampusLab.ms_campuslab_bookings.domain.BookingStatus;
import CampusLab.ms_campuslab_bookings.dto.BookingCreateRequest;
import CampusLab.ms_campuslab_bookings.dto.BookingResponse;
import CampusLab.ms_campuslab_bookings.dto.BookingStatusUpdateRequest;
import CampusLab.ms_campuslab_bookings.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingCreateRequest request) {
        BookingResponse response = bookingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(@PathVariable Long id,
                                                          @Valid @RequestBody BookingStatusUpdateRequest request) {
        return ResponseEntity.ok(bookingService.updateStatus(id, request.status()));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        BookingStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = BookingStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Estado invalido: " + status);
            }
        }
        return ResponseEntity.ok(bookingService.search(statusEnum, from, to));
    }
}
