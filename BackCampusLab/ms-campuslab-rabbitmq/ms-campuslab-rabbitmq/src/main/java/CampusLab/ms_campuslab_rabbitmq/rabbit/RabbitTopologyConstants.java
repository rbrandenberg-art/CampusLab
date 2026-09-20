package CampusLab.ms_campuslab_rabbitmq.rabbit;

/**
 * Nombres exactos de exchanges, colas, DLQ, routing keys y patrones de la
 * topología RabbitMQ de CampusLab (ver docs/campuslab-brief.md, sección 7).
 * <p>
 * Estos nombres deben permanecer IDÉNTICOS a los usados por los servicios
 * publicadores (ms-campuslab-bookings) y consumidores (ms-campuslab-notify).
 */
public final class RabbitTopologyConstants {

    private RabbitTopologyConstants() {
    }

    // Exchanges
    public static final String EXCHANGE_CMD_DIRECT = "cmd.direct";
    public static final String EXCHANGE_CMD_TOPIC = "cmd.topic";
    public static final String EXCHANGE_CMD_DEAD_DLX = "cmd.dead.dlx";

    // Colas principales
    public static final String QUEUE_EMAIL = "q.cmd.email";
    public static final String QUEUE_PREP = "q.cmd.prep";
    public static final String QUEUE_VOUCHER = "q.cmd.voucher";

    // Colas de dead-letter
    public static final String QUEUE_EMAIL_DLQ = "q.cmd.email.dlq";
    public static final String QUEUE_PREP_DLQ = "q.cmd.prep.dlq";
    public static final String QUEUE_VOUCHER_DLQ = "q.cmd.voucher.dlq";

    // Routing keys de binding directo
    public static final String ROUTING_KEY_EMAIL_SEND = "email.send";
    public static final String ROUTING_KEY_PREP_TICKET = "prep.ticket";
    public static final String ROUTING_KEY_VOUCHER_GEN = "voucher.gen";

    // Patrones de binding topic
    public static final String PATTERN_EMAIL = "email.*";
    public static final String PATTERN_PREP = "prep.#";
    public static final String PATTERN_VOUCHER = "voucher.*";

    // Routing keys de dead-letter (usadas como x-dead-letter-routing-key y en el binding a cmd.dead.dlx)
    public static final String DLQ_ROUTING_KEY_EMAIL = "email.dlq";
    public static final String DLQ_ROUTING_KEY_PREP = "prep.dlq";
    public static final String DLQ_ROUTING_KEY_VOUCHER = "voucher.dlq";
}
