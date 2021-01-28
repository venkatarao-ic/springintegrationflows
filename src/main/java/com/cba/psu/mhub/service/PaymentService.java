package com.cba.psu.mhub.service;

import com.cba.psu.mhub.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class PaymentService {


    @ServiceActivator(inputChannel = "integration.payment.jsonToObject.channel")
    public void processJsonToObject(Message<?> message) throws MessagingException {
       MessageChannel replyChannel = (MessageChannel) message.getHeaders().getReplyChannel();
        MessageBuilder.fromMessage(message);
        System.out.println("######################");
        System.out.println("Json to Object - " + message.getPayload());
        Payment payment = (Payment) message.getPayload();
        Message<?> newMessage = MessageBuilder.withPayload(payment.toString()).build();
       replyChannel.send(newMessage);
    }

    @ServiceActivator(inputChannel = "integration.payment.objectToJson.channel")
    public Message<?> recieveMessage1(Message<?> message) throws MessagingException {
        System.out.println("######################");
        System.out.println(message);
        System.out.println("######################");
        System.out.println("Object to Json - " + message.getPayload());
        return message;
    }


}
