package guru.sfg.beer.order.service.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.ValidateBeerOrderRequest;
import guru.sfg.brewery.model.events.ValidateOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void Listen(Message msg){
        Boolean isValid = true;
        Boolean sendResponse = true;

        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) msg.getPayload();

        if(request.getBeerOrderDto().getCustomerRef() != null){
             if(request.getBeerOrderDto().getCustomerRef().equals("fail-validation")){
                isValid = false;
            }else if(request.getBeerOrderDto().getCustomerRef().equals("don't-validate")){
                sendResponse = false;
            }
        }

        if(sendResponse){
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE, ValidateOrderResult.builder()
                    .isValid(isValid)
                    .orderId(request.getBeerOrderDto().getId())
                    .build());
        }
    }
}
