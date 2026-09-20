package CampusLab.ms_campuslab_bookings.exception;

/** Se lanza cuando se pide un salto de estado no permitido por la máquina de estados (sección 3 del brief). */
public class InvalidBookingStateTransitionException extends RuntimeException {
    public InvalidBookingStateTransitionException(String message) {
        super(message);
    }
}
