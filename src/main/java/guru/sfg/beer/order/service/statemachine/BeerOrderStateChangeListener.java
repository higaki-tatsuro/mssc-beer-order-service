package guru.sfg.beer.order.service.statemachine;

import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.domain.BeerOrderEventEnum;
import guru.sfg.beer.order.service.domain.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeerOrderStateChangeListener extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    // 状態遷移を検知し、DBに永続化する
    @Override
    public void preStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state, Message<BeerOrderEventEnum> message,
                               Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition, StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault(BeerOrderManagerImpl.ORDER_ID_HEADER, " ")))
                .ifPresent(orderId -> {
                    log.debug("Saving state for order id: ", orderId, " Status: " + state.getId());

                    BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(orderId));
                    beerOrder.setOrderStatus(state.getId());
                    // Hibernateの遅延ロードに影響を受けないよう、即DBに反映する
                    beerOrderRepository.saveAndFlush(beerOrder);
                });
    }
}
