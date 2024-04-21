package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.BeerOrderStateChangeListener;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "beer_order_id";
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;

    private final BeerOrderRepository repository;

    private final BeerOrderStateChangeListener listener;

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        // オーダーオブジェクトを完全に初期化
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = repository.save(beerOrder);
        // バリデーションを行うようStateMachineにイベントを通知
        this.sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATED_ORDER);

        return savedBeerOrder;
    }

    // バリデーション結果通知用メソッ
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        BeerOrder beerOrder = repository.findOneById(beerOrderId);

        if(isValid){
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            // イベント発生によりStateMachineの状態が更新されるため、最新の状態を取得するためにDBアクセスを行う
            BeerOrder validatedOrder = repository.findOneById(beerOrderId);
            sendBeerOrderEvent(validatedOrder, BeerOrderEventEnum.ALLOCATION_ORDER);
        }else{
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    // 状態遷移メッセージ送信用メソッド
    public void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = this.build(beerOrder);

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        stateMachine.sendEvent(msg);
    }


    // DBから取得したレコードを用いて、StateMachineの状態を復元するメソッド
    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder){
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = factory.getStateMachine(beerOrder.getId());

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(listener);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                });

        stateMachine.start();

        return stateMachine;
    }
}
