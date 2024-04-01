package com.example.consumer.message;

import com.example.consumer.dto.MessageDTO;
import com.example.consumer.service.ReadMessageService;
import com.example.consumer.utils.RabbitMQUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageConsumerEvent {

    private final ReadMessageService readMessageService;
    private final RabbitMQUtils rabbitMQUtils;


    @Value("${api.queue.message.exchange}")
    private String exchange;
    @Value("${api.queue.message.route}")
    private String route;
    @Value("${api.queue.delay}")
    private Integer defaultDelay;

    @RabbitListener(bindings =
    @QueueBinding(
            value = @Queue(
                    value = "${api.queue.message.queue}",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = "${api.queue.message.exchange}"),
                            @Argument(name = "x-dead-letter-routing-key", value = "${api.queue.message.dlq.route}")}),
            exchange = @Exchange(value = "${api.queue.message.exchange}", type = "x-delayed-message", arguments = @Argument(name = "x-delayed-type", value = "direct")),
            key = "${api.queue.message.route}"))
    public void receiveMessage(org.springframework.messaging.Message<MessageDTO> message) throws Exception {
        try {
            readMessageService.readMessage(message.getPayload().getMessage());
        } catch (Exception e) {
            rabbitMQUtils.handleException(message, defaultDelay, exchange, route);
        }
    }


    @RabbitListener(bindings =
    @QueueBinding(
            value = @Queue(
                    value = "${api.queue.message.dlq.queue}",
                    arguments = {
                            @Argument(name = "x-delay", value = "${api.queue.delay}", type = "java.lang.Integer")}),
            exchange = @Exchange(value = "${api.queue.message.exchange}", type = "x-delayed-message"),
            key = "${api.queue.message.dlq.route}"))
    public void receiveMessageDlq(org.springframework.messaging.Message<MessageDTO> message) throws JsonProcessingException {
        rabbitMQUtils.sendToMainQueue(message, exchange, route);
    }

}
