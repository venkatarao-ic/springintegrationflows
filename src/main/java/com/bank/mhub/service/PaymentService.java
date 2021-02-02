package com.bank.mhub.service;

import com.bank.mhub.model.Payment;
import org.springframework.integration.annotation.ServiceActivator;
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
        //Payment payment = (Payment) message.getPayload();
        Message<?> newMessage = MessageBuilder.withPayload(message.getPayload()).build();
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
