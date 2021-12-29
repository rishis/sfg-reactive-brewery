package guru.springframework.sfgrestbrewery.web.controller;

import guru.springframework.sfgrestbrewery.bootstrap.BeerLoader;
import guru.springframework.sfgrestbrewery.services.BeerService;
import guru.springframework.sfgrestbrewery.web.model.BeerDto;
import guru.springframework.sfgrestbrewery.web.model.BeerPagedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@WebFluxTest(BeerController.class)
class BeerControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockBean
    BeerService beerService;

    BeerDto validBeer;

    @BeforeEach
    void setUp() {
        validBeer = BeerDto.builder()
                .beerName("Test Beer")
                .beerStyle("Delhi Style")
                .upc(BeerLoader.BEER_1_UPC)
                .build();

    }
    @Test
    void getBeerById() {
        UUID beerId = UUID.randomUUID();
        given(beerService.getById(any(),any())).willReturn(validBeer);

        webTestClient.get()
                .uri("/api/v1/beer/"+beerId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(),equalTo(validBeer.getBeerName()));
        System.out.println("getBeerById() executed");
    }

    @Test
    void listBeers() {
        List<BeerDto> beerList = List.of(validBeer);
        BeerPagedList beerPagedList = new BeerPagedList(beerList, PageRequest.of(1,1), beerList.size());
        given(beerService.listBeers(any(),any(),any(),any())).willReturn(beerPagedList);

        webTestClient.get()
                .uri("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerPagedList.class)
                .value(beerDtos -> beerPagedList.getContent().get(0).getBeerName(),equalTo(validBeer.getBeerName()));

        System.out.println("listBeers() executed");
    }

    @Test
    void getBeerByUpc() {
        String upc = validBeer.getUpc();
        given(beerService.getByUpc(any())).willReturn(validBeer);

        webTestClient.get()
                .uri("/api/v1/beerUpc/"+upc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BeerDto.class)
                .value(beerDto -> beerDto.getBeerName(),equalTo(validBeer.getBeerName()));
        System.out.println("getBeerByUpc() executed");
    }


}