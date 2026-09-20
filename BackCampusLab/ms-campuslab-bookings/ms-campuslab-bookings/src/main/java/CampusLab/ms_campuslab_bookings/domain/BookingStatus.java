package CampusLab.ms_campuslab_bookings.domain;

/**
 * Estados de una reserva (sección 3 del brief compartido).
 * Máquina de estados: SOLICITADA -> APROBADA -> EN_PREPARACION -> EN_USO -> DEVUELTA,
 * y desde SOLICITADA o APROBADA también se puede pasar a CANCELADA.
 */
public enum BookingStatus {
    SOLICITADA,
    APROBADA,
    EN_PREPARACION,
    EN_USO,
    DEVUELTA,
    CANCELADA
}
