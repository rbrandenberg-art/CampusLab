package CampusLab.ms_campuslab_rabbitmq.web;

import CampusLab.ms_campuslab_rabbitmq.web.dto.BindingInfo;
import CampusLab.ms_campuslab_rabbitmq.web.dto.ExchangeInfo;
import CampusLab.ms_campuslab_rabbitmq.web.dto.QueueInfo;
import CampusLab.ms_campuslab_rabbitmq.web.dto.TopologyResponse;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Endpoint de solo lectura que documenta/verifica la topología RabbitMQ
 * declarada por {@link CampusLab.ms_campuslab_rabbitmq.rabbit.RabbitTopologyConfig}.
 * <p>
 * La respuesta se construye a partir de los beans realmente declarados
 * (no es un JSON hardcodeado aparte), por lo que refleja el estado real
 * de la topología que este servicio administra.
 */
@RestController
@RequestMapping("/api/admin/rabbitmq")
public class RabbitTopologyController {

    private final Declarables topology;

    public RabbitTopologyController(Declarables topology) {
        this.topology = topology;
    }

    @GetMapping("/topology")
    @PreAuthorize("hasRole('Admin')")
    public TopologyResponse getTopology() {
        List<ExchangeInfo> exchanges = new ArrayList<>();
        List<QueueInfo> queues = new ArrayList<>();
        List<BindingInfo> bindings = new ArrayList<>();

        for (Declarable declarable : topology.getDeclarables()) {
            if (declarable instanceof Exchange exchange) {
                exchanges.add(new ExchangeInfo(exchange.getName(), exchange.getType(), exchange.isDurable()));
            } else if (declarable instanceof Queue queue) {
                queues.add(new QueueInfo(queue.getName(), queue.isDurable(), queue.getArguments()));
            } else if (declarable instanceof Binding binding) {
                bindings.add(new BindingInfo(
                        binding.getExchange(),
                        binding.getDestination(),
                        binding.getDestinationType().name(),
                        binding.getRoutingKey()));
            }
        }

        return new TopologyResponse(exchanges, queues, bindings);
    }
}
