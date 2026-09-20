package CampusLab.ms_campuslab_rabbitmq.web.dto;

import java.util.List;

public record TopologyResponse(
        List<ExchangeInfo> exchanges,
        List<QueueInfo> queues,
        List<BindingInfo> bindings
) {
}
