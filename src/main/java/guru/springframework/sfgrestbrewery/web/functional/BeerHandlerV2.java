package guru.springframework.sfgrestbrewery.web.functional;

import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.controller.NotFoundException;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerHandlerV2 {

    private final BeerService beerService;
    private final Validator validator;

    public Mono<ServerResponse> getBeerById(ServerRequest serverRequest) {
            Integer beerId = Integer.valueOf(serverRequest.pathVariable("beerId"));
            Boolean showInventory = Boolean.valueOf(serverRequest.queryParam("showInventory").orElse("false"));

            return beerService.getById(beerId,showInventory)
                    .flatMap(beerDto -> {
                        return ServerResponse.ok().bodyValue(beerDto);
                    }).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getBeerByUPC(ServerRequest serverRequest) {
        String beerUpc = String.valueOf(serverRequest.pathVariable("beerUpc"));

        return beerService.getByUpc(beerUpc)
                .flatMap(beerDto -> {
                    return ServerResponse.ok().bodyValue(beerDto);
                }).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> saveBeer(ServerRequest request) {
        Mono<BeerDto> beerDto = request.bodyToMono(BeerDto.class).doOnNext(this::validate);
        System.out.println("Inside save beer method $$$$$$");
        return beerService.saveNewBeer(beerDto).flatMap(beerDto1 -> {
            return ServerResponse.ok().header("location","http://api.springframework.guru/api/v1/beer/"+beerDto1.getId())
                    .build();
        });

    }

    public void validate(BeerDto beerDto) {
        Errors errors = new BeanPropertyBindingResult(beerDto,"beerDto");
        validator.validate(beerDto,errors);
        System.out.println("Inside validate method ~~~~~~~~"+errors.hasErrors());
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }

    }

    public Mono<ServerResponse> deleteBeer(ServerRequest request) {

    /*    Integer beerId = Integer.valueOf(request.pathVariable("beerId"));
        System.out.println("Inside delete beer method $$$$$$");
        beerService.deleteBeerById(beerId);
        return ServerResponse.ok().build();
*/

        return beerService.deleteBeerById(Integer.valueOf(request.pathVariable("beerId")))
                .flatMap(voidMono -> {
                    return ServerResponse.ok().build();
                });

    }

}
