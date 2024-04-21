package guru.sfg.beer.order.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig{

    public static final String VALIDATE_ORDER_QUEUE = "validate-order";

    public static final String VALIDATE_ORDER_RESPONSE_QUEUE = "validate-order-response";

    @Bean
    public MessageConverter jacksonJmsMessageConverter(){
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTypeIdPropertyName("_type");
        messageConverter.setTargetType(MessageType.TEXT);

        return  messageConverter;
    }

}
