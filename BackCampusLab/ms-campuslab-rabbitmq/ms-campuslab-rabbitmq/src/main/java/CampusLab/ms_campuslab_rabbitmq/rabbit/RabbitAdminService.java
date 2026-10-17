package CampusLab.ms_campuslab_rabbitmq.rabbit;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    // Spring inyecta automáticamente AmqpAdmin, que es la herramienta oficial para modificar RabbitMQ
    public RabbitAdminService(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    public void createQueue(String queueName, boolean durable) {
        Queue queue = durable ? QueueBuilder.durable(queueName).build() : QueueBuilder.nonDurable(queueName).build();
        amqpAdmin.declareQueue(queue);
    }

    public boolean deleteQueue(String queueName) {
        // Elimina la cola. Devuelve true si existía y se eliminó.
        return amqpAdmin.deleteQueue(queueName);
    }
}