package guru.sfg.beer.order.service.testcomponents;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderAllocationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(Message msg){
        Boolean allocationError = false;
        Boolean pendingInventory = false;

        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();

        if(request.getBeerOrderDto().getCustomerRef() != null){
            if(request.getBeerOrderDto().getCustomerRef().equals("fail-allocation")){
                allocationError = true;
            }
            if(request.getBeerOrderDto().getCustomerRef().equals("partial-allocation")){
                pendingInventory = true;
            }
        }

        Boolean finalPendingInventory = pendingInventory;
        request.getBeerOrderDto().getBeerOrderLines().forEach(beerOrderLineDto -> {
            if(finalPendingInventory){
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
            }else {
                beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
            }
        });

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE, AllocateOrderResult.builder()
                .beerOrderDto(request.getBeerOrderDto())
                .allocationError(allocationError)
                .pendingInventory(pendingInventory)
                .build());
    }

}
