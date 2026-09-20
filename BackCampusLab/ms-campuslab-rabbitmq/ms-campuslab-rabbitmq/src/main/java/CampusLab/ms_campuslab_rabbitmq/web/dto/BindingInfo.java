package CampusLab.ms_campuslab_rabbitmq.web.dto;

public record BindingInfo(String exchange, String destination, String destinationType, String routingKey) {
}
