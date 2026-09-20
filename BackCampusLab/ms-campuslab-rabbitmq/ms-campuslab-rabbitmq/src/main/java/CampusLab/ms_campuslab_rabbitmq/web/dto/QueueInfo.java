package CampusLab.ms_campuslab_rabbitmq.web.dto;

import java.util.Map;

public record QueueInfo(String name, boolean durable, Map<String, Object> arguments) {
}
