package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TICKETTI_EXCHANGE = "ticketti.exchange";
    public static final String EVENTOS_EXCHANGE = "eventos.exchange";

    public static final String QUEUE_PAGO_APROBADO = "pago.aprobado";
    public static final String QUEUE_COMPRA_REVERTIDA = "compra.revertida";
    public static final String QUEUE_MENSAJERIA = "mensajeria.queue";
    public static final String QUEUE_EVENTOS = "eventos.cola";

    public static final String ROUTING_KEY_PAGO_APROBADO = "pago.aprobado";
    public static final String ROUTING_KEY_COMPRA_REVERTIDA = "compra.revertida";
    public static final String ROUTING_KEY_MENSAJERIA = "pago.aprobado";
    public static final String ROUTING_KEY_EVENTOS = "eventos.exchange";

    @Bean
    public DirectExchange tickettiExchange() {
        return new DirectExchange(TICKETTI_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange eventosExchange() {
        return new DirectExchange(EVENTOS_EXCHANGE, true, false);
    }

    @Bean
    public Queue queuePagoAprobado() {
        return QueueBuilder.durable(QUEUE_PAGO_APROBADO).build();
    }

    @Bean
    public Queue queueCompraRevertida() {
        return QueueBuilder.durable(QUEUE_COMPRA_REVERTIDA).build();
    }

    @Bean
    public Queue queueMensajeria() {
        return QueueBuilder.durable(QUEUE_MENSAJERIA).build();
    }

    @Bean
    public Queue queueEventos() {
        return QueueBuilder.durable(QUEUE_EVENTOS).build();
    }

    @Bean
    public Binding bindingPagoAprobado(Queue queuePagoAprobado, DirectExchange tickettiExchange) {
        return BindingBuilder.bind(queuePagoAprobado).to(tickettiExchange).with(ROUTING_KEY_PAGO_APROBADO);
    }

    @Bean
    public Binding bindingCompraRevertida(Queue queueCompraRevertida, DirectExchange tickettiExchange) {
        return BindingBuilder.bind(queueCompraRevertida).to(tickettiExchange).with(ROUTING_KEY_COMPRA_REVERTIDA);
    }

    @Bean
    public Binding bindingMensajeria(Queue queueMensajeria, DirectExchange tickettiExchange) {
        return BindingBuilder.bind(queueMensajeria).to(tickettiExchange).with(ROUTING_KEY_MENSAJERIA);
    }

    @Bean
    public Binding bindingEventos(Queue queueEventos, DirectExchange eventosExchange) {
        return BindingBuilder.bind(queueEventos).to(eventosExchange).with(ROUTING_KEY_EVENTOS);
    }

    @Bean
    @SuppressWarnings("removal")
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @SuppressWarnings({"null", "removal"})
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    @SuppressWarnings({"null", "removal"})
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}