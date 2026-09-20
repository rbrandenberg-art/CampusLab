package CampusLab.ms_campuslab_kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara la topologia Kafka de CampusLab (ver docs/campuslab-brief.md, seccion 8).
 *
 * Este es el UNICO microservicio responsable de crear los topicos: los demas
 * servicios (bookings, audit, catalog, report) solo publican/consumen asumiendo
 * que ya existen. Los nombres deben coincidir EXACTAMENTE con los que ya
 * asumen esos servicios: {@code bookings.events} y {@code audit.timeline}.
 *
 * Spring Kafka registra automaticamente cualquier bean {@link NewTopic} del
 * contexto contra el broker configurado (via {@code KafkaAdmin}) al arrancar.
 */
@Configuration
public class KafkaTopologyConfig {

    private static final int PARTITIONS = 3;

    private static final long RETENTION_7_DAYS_MS = 7L * 24 * 60 * 60 * 1000;
    private static final long RETENTION_30_DAYS_MS = 30L * 24 * 60 * 60 * 1000;
    private static final long RETENTION_14_DAYS_MS = 14L * 24 * 60 * 60 * 1000;

    /**
     * El enunciado pide factor de replicas = 3, pero un entorno de desarrollo
     * con un solo broker Kafka no puede satisfacer eso (fallaria la creacion
     * del topico). Se deja configurable via KAFKA_REPLICATION_FACTOR con
     * default 1 para desarrollo local; EN PRODUCCION debe fijarse en 3.
     */
    @Value("${KAFKA_REPLICATION_FACTOR:1}")
    private int replicationFactor;

    @Bean
    public NewTopic bookingsEventsTopic() {
        return TopicBuilder.name("bookings.events")
                .partitions(PARTITIONS)
                .replicas(replicationFactor)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(RETENTION_7_DAYS_MS))
                .build();
    }

    @Bean
    public NewTopic auditTimelineTopic() {
        return TopicBuilder.name("audit.timeline")
                .partitions(PARTITIONS)
                .replicas(replicationFactor)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG,
                        TopicConfig.CLEANUP_POLICY_COMPACT + "," + TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(RETENTION_30_DAYS_MS))
                .build();
    }

    @Bean
    public NewTopic bookingsEventsDltTopic() {
        return TopicBuilder.name("bookings.events.DLT")
                .partitions(PARTITIONS)
                .replicas(replicationFactor)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(RETENTION_14_DAYS_MS))
                .build();
    }

    @Bean
    public NewTopic auditTimelineDltTopic() {
        return TopicBuilder.name("audit.timeline.DLT")
                .partitions(PARTITIONS)
                .replicas(replicationFactor)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(RETENTION_14_DAYS_MS))
                .build();
    }
}
