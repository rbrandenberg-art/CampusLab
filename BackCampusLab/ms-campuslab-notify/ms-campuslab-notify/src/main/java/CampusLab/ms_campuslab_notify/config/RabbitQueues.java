package CampusLab.ms_campuslab_notify.config;

/**
 * Nombres de colas de la topología RabbitMQ (sección 7 del brief compartido).
 * Estos nombres son un contrato entre microservicios: deben coincidir
 * exactamente con los declarados por ms-campuslab-rabbitmq y usados por
 * ms-campuslab-bookings al publicar.
 */
public final class RabbitQueues {

    public static final String EMAIL = "q.cmd.email";
    public static final String PREP = "q.cmd.prep";
    public static final String VOUCHER = "q.cmd.voucher";

    private RabbitQueues() {
    }
}
