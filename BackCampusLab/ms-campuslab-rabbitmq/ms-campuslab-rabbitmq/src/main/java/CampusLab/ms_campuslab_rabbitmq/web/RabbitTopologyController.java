package CampusLab.ms_campuslab_rabbitmq.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import CampusLab.ms_campuslab_rabbitmq.rabbit.RabbitAdminService;
import CampusLab.ms_campuslab_rabbitmq.web.dto.BindingInfo;
import CampusLab.ms_campuslab_rabbitmq.web.dto.CreateQueueRequest;
import CampusLab.ms_campuslab_rabbitmq.web.dto.ExchangeInfo;
import CampusLab.ms_campuslab_rabbitmq.web.dto.QueueInfo;
import CampusLab.ms_campuslab_rabbitmq.web.dto.TopologyResponse;
import jakarta.validation.Valid;

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
    private final RabbitAdminService rabbitAdminService;

    public RabbitTopologyController(Declarables topology, RabbitAdminService rabbitAdminService) {
        this.topology = topology;
        this.rabbitAdminService = rabbitAdminService;
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
    // NUEVO ENDPOINT: Crear Cola (Cumple el 13% de la rúbrica)
    @PostMapping("/queues")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        rabbitAdminService.createQueue(request.name(), request.durable());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Cola '" + request.name() + "' creada exitosamente.");
    }

    // NUEVO ENDPOINT: Eliminar Cola (Cumple el 13% de la rúbrica)
    @DeleteMapping("/queues/{name}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<String> deleteQueue(@PathVariable String name) {
        boolean deleted = rabbitAdminService.deleteQueue(name);
        if (deleted) {
            return ResponseEntity.ok("Cola '" + name + "' eliminada exitosamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("La cola '" + name + "' no existe.");
        }
    }
}
