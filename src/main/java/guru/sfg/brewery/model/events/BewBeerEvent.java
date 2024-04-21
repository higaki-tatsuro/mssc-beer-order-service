package guru.sfg.brewery.model.events;

import guru.sfg.brewery.model.BeerDto;
import guru.sfg.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BewBeerEvent {
    static final long serialVersionUID = 1859508991796861578L;

    private BeerOrderDto beerOrderDto;
}
