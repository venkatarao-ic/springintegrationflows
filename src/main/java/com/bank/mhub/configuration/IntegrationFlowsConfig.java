package com.bank.mhub.configuration;

import com.bank.mhub.model.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.Transformers;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerProperties;

import javax.jms.ConnectionFactory;
import java.util.Map;



@Configuration
@EnableIntegration
@IntegrationComponentScan
@Import({KafkaProducingChannelConfig.class})
public class IntegrationFlowsConfig {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationFlowsConfig.class);

    private static String SPRING_INTEGRATION_KAFKA_TOPIC = "spring-integration-kafka.t";

    @Value("${mq.queue.payment.in}")
    private String paymentInQueue;

    @Value("${mq.queue.payment.out}")
    private String paymentOutQueue;

    @Value("${mq.queue.payment.in2}")
    private String paymentInQueue2;

    @Value("${kafka.topic.payment.in}")
    private String kafkaPaymentIn;

    @Value("${kafka.topic.payment.out}")
    private String kafkaPaymentOut;

    @Value("${kafka.topic.payment.in2}")
    private String kafkaPaymentIn2;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    @Qualifier("kafkaConsumerFactory")
    private ConsumerFactory<String, String> consumerFactory;

    @Autowired
    @Qualifier("kafkaConsumerConfigs")
    private Map<String, Object> consumerConfigs;

    @Autowired
    private JmsTemplate jmsTemplate;


    /**
     * Active MQ to Active MQ through JMS
     * @param connectionFactory
     * @return
     */
    @Bean
    public IntegrationFlow flowMQToMQ(ConnectionFactory connectionFactory) {
        return IntegrationFlows.from(Jms.inboundAdapter(connectionFactory)
                .destination(paymentInQueue), e -> e.poller(Pollers
                .fixedDelay(5000)
                .maxMessagesPerPoll(2)))
                .transform(Transformers.fromJson(Payment.class, new Jackson2JsonObjectMapper(objectMapper())))
                .transform(Transformers.toJson())
                .handle(Jms.outboundAdapter(connectionFactory)
                        .destination(paymentOutQueue))
                .get();
    }

    /**
     * Flow From KAFKA To KAFKA TOPIC
     * @param connectionFactory
     * @return
     */
    @Bean
    public IntegrationFlow flowKafkaToKafka(ConnectionFactory connectionFactory) {
        return IntegrationFlows
                .from(Kafka.inboundChannelAdapter(consumerFactory,new ConsumerProperties(kafkaPaymentIn)), e -> e.poller(Pollers
                        .fixedDelay(5000)
                        .maxMessagesPerPoll(2)))
                .transform(Transformers.fromJson(Payment.class, new Jackson2JsonObjectMapper(objectMapper())))
                .transform(Transformers.toJson())
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate).topic(kafkaPaymentOut))
                .get();
    }

    /**
     * Flow From Active MQ To KAFKA TOPIC
     * @param connectionFactory
     * @return
     */
    @Bean
    public IntegrationFlow flowMQToKafka(ConnectionFactory connectionFactory) {
        return IntegrationFlows.from(Jms.inboundAdapter(connectionFactory)
                .destination(paymentInQueue2), e -> e.poller(Pollers
                .fixedDelay(5000)
                .maxMessagesPerPoll(2)))
                .channel("integration.payment.jsonToObject.channel")
                .channel("integration.payment.objectToJson.channel")
                .handle(Kafka.outboundChannelAdapter(kafkaTemplate).topic(SPRING_INTEGRATION_KAFKA_TOPIC))
                .get();
    }

    /**
     * Flow From KAFKA To MQ
     * @param connectionFactory
     * @return
     */
    @Bean
    public IntegrationFlow flowKafkaToMQ(ConnectionFactory connectionFactory) {
        return IntegrationFlows
                .from(Kafka.inboundChannelAdapter(consumerFactory,new ConsumerProperties(kafkaPaymentIn2)), e -> e.poller(Pollers
                        .fixedDelay(5000)
                        .maxMessagesPerPoll(2)))
                .transform(Transformers.fromJson(Payment.class, new Jackson2JsonObjectMapper(objectMapper())))
                .transform(Transformers.toJson())
                .handle(Jms.outboundAdapter(connectionFactory)
                        .destination(paymentOutQueue))
                .get();
    }
    @JmsListener(destination = "DEV.QUEUE.2")
    public void listen(String in) {
        logger.info("Message is being processed " + in);
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}
