package com.bank.mhub.controller;


import com.bank.mhub.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mq.queue.payment.in:DEV.QUEUE.1}")
    private String paymentInQueue;

    @Value("${mq.queue.payment.out:DEV.QUEUE.2}")
    private String paymentOutQueue;

    @Value("${kafka.topic.payment.in:kafka.payment.in}")
    private String kafkaPaymentIn;

    @Value("${kafka.topic.payment.out:kafka.payment.out}")
    private String kafkaPaymentOut;

    @Value("${kafka.topic.payment.in2:kafka.payment.in2}")
    private String kafkaPaymentIn2;

    @Autowired
    private ApplicationContext context;

    @PostMapping("/queue/payment")
    public Payment sendMessageToQueue(@RequestBody Payment payment) throws IOException {
        System.out.println(objectMapper);
        String message = objectMapper.writeValueAsString(payment);
        System.out.println(message);
        jmsTemplate.convertAndSend(paymentInQueue, message);
        return payment;
    }

    @PostMapping("/kafka/payment")
    public Payment sendMessageToKafka(@RequestBody Payment payment) throws IOException {
        String data = objectMapper.writeValueAsString(payment);
        System.out.println(data);
        MessageChannel producingChannel = context.getBean("producingChannel", MessageChannel.class);
        Map<String, Object> headers = Collections.singletonMap(KafkaHeaders.TOPIC, kafkaPaymentIn);
        for (int i = 0; i < 10; i++) {
            GenericMessage<String> message = new GenericMessage<>(data, headers);
            producingChannel.send(message);
            logger.info("sent message='{}'", message);
            return payment;
        }
        return payment;
    }

    @PostMapping("/kafka/payment2")
    public Payment sendMessageToKafka2(@RequestBody Payment payment) throws IOException {
        String data = objectMapper.writeValueAsString(payment);
        System.out.println(data);
        MessageChannel producingChannel = context.getBean("producingChannel", MessageChannel.class);
        Map<String, Object> headers = Collections.singletonMap(KafkaHeaders.TOPIC, kafkaPaymentIn2);
        for (int i = 0; i < 10; i++) {
            GenericMessage<String> message = new GenericMessage<>(data, headers);
            producingChannel.send(message);
            logger.info("sent message='{}'", message);
            return payment;
        }
        return payment;
    }
}