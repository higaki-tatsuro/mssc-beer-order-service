package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.brewery.model.events.AllocateOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderAllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResult allocateOrderResult){
        // 割り当て成功&在庫不足なし
        if(!allocateOrderResult.getAllocationError() && !allocateOrderResult.getPendingInventory()){
            beerOrderManager.beerOrderAllocationPassed(allocateOrderResult.getBeerOrderDto());
        // 在庫不足あり
        }else if(!allocateOrderResult.getAllocationError() && allocateOrderResult.getPendingInventory()){
            beerOrderManager.beerOrderAllocationPendingInventory(allocateOrderResult.getBeerOrderDto());
        // 割り当てエラーあり
        }else if(allocateOrderResult.getAllocationError()){
            beerOrderManager.beerOrderAllocationFailed(allocateOrderResult.getBeerOrderDto());
        }
    }
}
