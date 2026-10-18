package service.comisiones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "realtyhub.events";
    public static final String DEAD_LETTER_EXCHANGE = "realtyhub.events.dlx";

    public static final String RK_CONTRACT_SIGNED = "contract.signed";
    public static final String RK_CONTRACT_CANCELLED = "contract.cancelled";

    public static final String RK_COMMISSION_CREATED = "commission.created";
    public static final String RK_COMMISSION_APPROVED = "commission.approved";
    public static final String RK_COMMISSION_PAID = "commission.paid";
    public static final String RK_COMMISSION_ANNULLED = "commission.annulled";
    public static final String RK_COMMISSION_REVERTED = "commission.reverted";

    public static final String QUEUE_CONTRACT_SIGNED = "comisiones.contract-signed";
    public static final String QUEUE_CONTRACT_SIGNED_DLQ = "comisiones.contract-signed.dlq";
    public static final String QUEUE_CONTRACT_CANCELLED = "comisiones.contract-cancelled";
    public static final String QUEUE_CONTRACT_CANCELLED_DLQ = "comisiones.contract-cancelled.dlq";

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue contractSignedQueue() {
        return QueueBuilder.durable(QUEUE_CONTRACT_SIGNED)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(QUEUE_CONTRACT_SIGNED_DLQ)
                .build();
    }

    @Bean
    public Queue contractSignedDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_CONTRACT_SIGNED_DLQ).build();
    }

    @Bean
    public Binding contractSignedBinding(Queue contractSignedQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(contractSignedQueue).to(eventsExchange).with(RK_CONTRACT_SIGNED);
    }

    @Bean
    public Binding contractSignedDeadLetterBinding(Queue contractSignedDeadLetterQueue,
                                                   DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(contractSignedDeadLetterQueue)
                .to(deadLetterExchange)
                .with(QUEUE_CONTRACT_SIGNED_DLQ);
    }

    @Bean
    public Queue contractCancelledQueue() {
        return QueueBuilder.durable(QUEUE_CONTRACT_CANCELLED)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(QUEUE_CONTRACT_CANCELLED_DLQ)
                .build();
    }

    @Bean
    public Queue contractCancelledDeadLetterQueue() {
        return QueueBuilder.durable(QUEUE_CONTRACT_CANCELLED_DLQ).build();
    }

    @Bean
    public Binding contractCancelledBinding(Queue contractCancelledQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(contractCancelledQueue).to(eventsExchange).with(RK_CONTRACT_CANCELLED);
    }

    @Bean
    public Binding contractCancelledDeadLetterBinding(Queue contractCancelledDeadLetterQueue,
                                                      DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(contractCancelledDeadLetterQueue)
                .to(deadLetterExchange)
                .with(QUEUE_CONTRACT_CANCELLED_DLQ);
    }
}
