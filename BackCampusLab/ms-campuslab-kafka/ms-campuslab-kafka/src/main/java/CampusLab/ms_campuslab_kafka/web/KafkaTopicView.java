package CampusLab.ms_campuslab_kafka.web;

/**
 * Representacion de solo lectura de un topico Kafka declarado por este
 * servicio, para el endpoint de documentacion/verificacion.
 */
public record KafkaTopicView(
        String name,
        int partitions,
        int replicationFactor,
        String cleanupPolicy,
        long retentionMs,
        String retentionHuman) {
}
