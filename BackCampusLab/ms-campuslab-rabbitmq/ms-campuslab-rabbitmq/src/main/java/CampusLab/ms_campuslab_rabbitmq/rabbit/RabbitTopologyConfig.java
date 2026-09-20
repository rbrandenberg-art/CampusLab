package CampusLab.ms_campuslab_rabbitmq.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static CampusLab.ms_campuslab_rabbitmq.rabbit.RabbitTopologyConstants.*;

/**
 * Declara la topología RabbitMQ completa de CampusLab: exchanges, colas
 * principales (con dead-lettering), colas DLQ y bindings.
 * <p>
 * Este microservicio es el ÚNICO responsable de declarar (crear) esta
 * topología. Los demás servicios (bookings, notify) solo publican/consumen
 * asumiendo que ya existe.
 */
@Configuration
public class RabbitTopologyConfig {

    @Bean
    public DirectExchange cmdDirectExchange() {
        return new DirectExchange(EXCHANGE_CMD_DIRECT, true, false);
    }

    @Bean
    public TopicExchange cmdTopicExchange() {
        return new TopicExchange(EXCHANGE_CMD_TOPIC, true, false);
    }

    @Bean
    public DirectExchange cmdDeadLetterExchange() {
        return new DirectExchange(EXCHANGE_CMD_DEAD_DLX, true, false);
    }

    @Bean
    public Queue emailQueue() {
        return mainQueue(QUEUE_EMAIL, DLQ_ROUTING_KEY_EMAIL);
    }

    @Bean
    public Queue prepQueue() {
        return mainQueue(QUEUE_PREP, DLQ_ROUTING_KEY_PREP);
    }

    @Bean
    public Queue voucherQueue() {
        return mainQueue(QUEUE_VOUCHER, DLQ_ROUTING_KEY_VOUCHER);
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(QUEUE_EMAIL_DLQ).build();
    }

    @Bean
    public Queue prepDlq() {
        return QueueBuilder.durable(QUEUE_PREP_DLQ).build();
    }

    @Bean
    public Queue voucherDlq() {
        return QueueBuilder.durable(QUEUE_VOUCHER_DLQ).build();
    }

    @Bean
    public Declarables rabbitTopology() {
        DirectExchange cmdDirect = cmdDirectExchange();
        TopicExchange cmdTopic = cmdTopicExchange();
        DirectExchange cmdDeadDlx = cmdDeadLetterExchange();

        Queue qEmail = emailQueue();
        Queue qPrep = prepQueue();
        Queue qVoucher = voucherQueue();

        Queue qEmailDlq = emailDlq();
        Queue qPrepDlq = prepDlq();
        Queue qVoucherDlq = voucherDlq();

        Binding emailDirectBinding = BindingBuilder.bind(qEmail).to(cmdDirect).with(ROUTING_KEY_EMAIL_SEND);
        Binding emailTopicBinding = BindingBuilder.bind(qEmail).to(cmdTopic).with(PATTERN_EMAIL);

        Binding prepDirectBinding = BindingBuilder.bind(qPrep).to(cmdDirect).with(ROUTING_KEY_PREP_TICKET);
        Binding prepTopicBinding = BindingBuilder.bind(qPrep).to(cmdTopic).with(PATTERN_PREP);

        Binding voucherDirectBinding = BindingBuilder.bind(qVoucher).to(cmdDirect).with(ROUTING_KEY_VOUCHER_GEN);
        Binding voucherTopicBinding = BindingBuilder.bind(qVoucher).to(cmdTopic).with(PATTERN_VOUCHER);

        Binding emailDlqBinding = BindingBuilder.bind(qEmailDlq).to(cmdDeadDlx).with(DLQ_ROUTING_KEY_EMAIL);
        Binding prepDlqBinding = BindingBuilder.bind(qPrepDlq).to(cmdDeadDlx).with(DLQ_ROUTING_KEY_PREP);
        Binding voucherDlqBinding = BindingBuilder.bind(qVoucherDlq).to(cmdDeadDlx).with(DLQ_ROUTING_KEY_VOUCHER);

        return new Declarables(
                cmdDirect, cmdTopic, cmdDeadDlx,
                qEmail, qPrep, qVoucher,
                qEmailDlq, qPrepDlq, qVoucherDlq,
                emailDirectBinding, emailTopicBinding,
                prepDirectBinding, prepTopicBinding,
                voucherDirectBinding, voucherTopicBinding,
                emailDlqBinding, prepDlqBinding, voucherDlqBinding
        );
    }

    private Queue mainQueue(String name, String dlqRoutingKey) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", EXCHANGE_CMD_DEAD_DLX)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }
}
