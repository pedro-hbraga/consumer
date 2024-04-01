package com.example.consumer.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static org.springframework.amqp.core.MessageBuilder.withBody;
import static org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON;
import static org.springframework.amqp.core.MessagePropertiesBuilder.newInstance;
import static org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer.X_EXCEPTION_STACKTRACE;

@Component
@RequiredArgsConstructor
public class RabbitMQUtils {

    private static final String X_DELAY = "x-delay";
    private static final String X_RETRIES_HEADER = "x-retries";
    private static final int MAX_NUMBER_OF_RETRIES = 3;

    private final RabbitTemplate rabbitTemplate;

    public void sendToMainQueue(Message<?> message, String exchange, String route) throws JsonProcessingException {
        int retries = this.getRetriesHeader(message) + 1;

        MessageProperties props = MessagePropertiesBuilder.newInstance().setContentType(CONTENT_TYPE_JSON).build();
        props.setHeader(X_RETRIES_HEADER, retries);

        ObjectMapper mapper = new ObjectMapper();
        org.springframework.amqp.core.Message newMessage = MessageBuilder.withBody(mapper.writeValueAsBytes(message.getPayload())).andProperties(props).build();

        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(route);
        rabbitTemplate.convertAndSend(newMessage);
    }

    public void handleException(Message<?> message, Integer defaultDelay, String exchange, String route) throws JsonProcessingException {
        int retriesHeader = getRetriesHeader(message);

        if (retriesHeader >= MAX_NUMBER_OF_RETRIES) {
            sendToParkingLot(message, exchange, route);
        }else {
            sendToDlq(message, defaultDelay, exchange, route);
        }
    }

    private Integer getRetriesHeader(Message<?> message) {

        Integer retriesHeader = (Integer) message.getHeaders().get(X_RETRIES_HEADER);

        if (retriesHeader == null) {
            return 0;
        }

        return retriesHeader;
    }

    private void sendToDlq(Message<?> message, Integer defaultDelay, String exchange, String route) throws JsonProcessingException {
        int retries = this.getRetriesHeader(message);

        MessageProperties props = MessagePropertiesBuilder.newInstance().setContentType(CONTENT_TYPE_JSON).build();
        props.setHeader(X_DELAY, defaultDelay);
        props.setHeader(X_RETRIES_HEADER, retries);

        ObjectMapper mapper = new ObjectMapper();
        org.springframework.amqp.core.Message newMessage = MessageBuilder.withBody(mapper.writeValueAsBytes(message.getPayload())).andProperties(props).build();

        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(route + ".dlq");
        rabbitTemplate.convertAndSend(newMessage);
    }

    private void sendToParkingLot(Message<?> message, String exchange, String route) throws JsonProcessingException {
        rabbitTemplate.setExchange(exchange);
        rabbitTemplate.setRoutingKey(route + ".parkingLot");

        rabbitTemplate.convertAndSend(withBody(new ObjectMapper().writeValueAsBytes(message.getPayload()))
                .andProperties(newInstance()
                        .setContentType(CONTENT_TYPE_JSON)
                        .build())
                .build());
    }


}
