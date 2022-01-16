package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.web.functional.BeerRouterConfig;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jt on 4/11/21.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientV2IT {
    public static final String BASE_URL = "http://localhost:8080";

    WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .build();
    }

    @Test
    void getBeerById() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BeerRouterConfig.BEER_V2_URL + "/" + 1)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {
            assertThat(beer).isNotNull();
            assertThat(beer.getBeerName()).isNotNull();

            countDownLatch.countDown();
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getBeerByIdNotFound() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri(BeerRouterConfig.BEER_V2_URL + "/" + 1333)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beer -> {

        }, throwable -> {
            countDownLatch.countDown();
        });

        countDownLatch.await(2000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void getBeerByUPC() throws InterruptedException {
        String upc = BeerLoader.BEER_1_UPC;

        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<BeerDto> beerDtoMono = webClient.get().uri("/api/v2/beerUpc/" + upc)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(BeerDto.class);

        beerDtoMono.subscribe(beerDto -> {
            assertThat(beerDto).isNotNull();
            assertThat(beerDto.getBeerName()).isNotNull();
            countDownLatch.countDown();

        });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testSaveBeer() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
                .beerName("JTs Beer")
                .upc("1233455")
                .beerStyle("PALE_ALE")
                .price(new BigDecimal("8.99"))
                .build();

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri("/api/v2/beer")
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(beerDto))
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).subscribe(responseEntity -> {

            assertThat(responseEntity.getStatusCode().is2xxSuccessful());

            countDownLatch.countDown();
        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test
    void testSaveBeerError() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        BeerDto beerDto = BeerDto.builder()
               // .beerName("JTs Beer")
                .upc("1233455")
              //  .beerStyle("PALE_ALE")
                .price(new BigDecimal("8.99"))
                .build();

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.post().uri("/api/v2/beer")
                .accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(beerDto))
                .retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).doOnError(throwable -> {
            countDownLatch.countDown();

        }).subscribe(responseEntity -> {

          //  assertThat(responseEntity.getStatusCode().isError());


        });

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }

    @Test void testDeleteById() throws InterruptedException{
        Integer beerToBeDeleted = Integer.valueOf("1");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Mono<ResponseEntity<Void>> beerResponseMono = webClient.delete().uri(BeerRouterConfig.BEER_V2_URL + "/"+beerToBeDeleted.toString())
                .accept(MediaType.APPLICATION_JSON).retrieve().toBodilessEntity();

        beerResponseMono.publishOn(Schedulers.parallel()).doOnError(throwable -> {

        }).subscribe(responseEntity -> {

            assertThat(responseEntity.getStatusCode().is2xxSuccessful());
            //  assertThat(responseEntity.getStatusCode().isError());
            countDownLatch.countDown();



        });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }

    @Test
    void deleteBeerByIDV12 () throws InterruptedException{
        CountDownLatch countDownLatch = new CountDownLatch(1);

        webClient.delete().uri(BeerRouterConfig.BEER_V2_URL + "/" + "1")
                .accept(MediaType.APPLICATION_JSON).retrieve().toBodilessEntity()
                .flatMap(responseEntity -> {
                            return webClient.get().uri(BeerRouterConfig.BEER_V2_URL + "/" + "1")
                                    .accept(MediaType.APPLICATION_JSON)
                                    .retrieve().bodyToMono(BeerDto.class);})
                .subscribe(savedDto -> {

                                    }, throwable -> {
                                        countDownLatch.countDown();
                                    });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(countDownLatch.getCount()).isEqualTo(0);

    }

    @Test
    void testDeleteBeer11() throws InterruptedException{
        Integer beerId = 3;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        webClient.delete().uri("/api/v2/beer/" + beerId )
                .retrieve().toBodilessEntity()
                .flatMap(responseEntity -> {
                    countDownLatch.countDown();

                    return webClient.get().uri("/api/v2/beer/" + beerId)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve().bodyToMono(BeerDto.class);
                }) .subscribe(savedDto -> {

        }, throwable -> {
            countDownLatch.countDown();
        });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }


}
