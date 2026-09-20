package CampusLab.ms_campuslab_kafka.web;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint de solo lectura para verificar/documentar la topologia Kafka que
 * este servicio declara al arrancar (ver CampusLab.ms_campuslab_kafka.config.KafkaTopologyConfig).
 * No crea ni modifica nada: solo refleja los beans NewTopic ya registrados
 * en el contexto de Spring.
 *
 * Protegido para rol Admin (ver CampusLab.ms_campuslab_kafka.config.SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin/kafka")
public class KafkaTopicsController {

    private final List<NewTopic> topics;

    public KafkaTopicsController(List<NewTopic> topics) {
        this.topics = topics;
    }

    @GetMapping("/topics")
    public List<KafkaTopicView> listTopics() {
        return topics.stream()
                .map(this::toView)
                .sorted(Comparator.comparing(KafkaTopicView::name))
                .toList();
    }

    private KafkaTopicView toView(NewTopic topic) {
        String cleanupPolicy = topic.configs() == null ? ""
                : topic.configs().getOrDefault(TopicConfig.CLEANUP_POLICY_CONFIG, "");
        long retentionMs = topic.configs() == null ? -1
                : Long.parseLong(topic.configs().getOrDefault(TopicConfig.RETENTION_MS_CONFIG, "-1"));
        return new KafkaTopicView(
                topic.name(),
                topic.numPartitions(),
                topic.replicationFactor(),
                cleanupPolicy,
                retentionMs,
                humanizeRetention(retentionMs));
    }

    private String humanizeRetention(long retentionMs) {
        if (retentionMs < 0) {
            return "sin definir";
        }
        long days = Duration.ofMillis(retentionMs).toDays();
        return days + " dias";
    }
}
