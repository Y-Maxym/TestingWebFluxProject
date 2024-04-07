package com.testing.maxym.qafordevsreactive.rest;

import com.testing.maxym.qafordevsreactive.dto.DeveloperDto;
import com.testing.maxym.qafordevsreactive.entity.DeveloperEntity;
import com.testing.maxym.qafordevsreactive.exception.DeveloperNotFoundException;
import com.testing.maxym.qafordevsreactive.exception.DeveloperWithEmailAlreadyExistsException;
import com.testing.maxym.qafordevsreactive.service.DeveloperService;
import com.testing.maxym.qafordevsreactive.util.DataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ComponentScan({"com.testing.maxym.qafordevsreactive.errorhandling"})
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = DeveloperRestControllerV1.class)
public class DeveloperRestControllerV1Tests {

    @Autowired
    private WebTestClient client;

    @MockBean
    private DeveloperService developerService;

    @Test
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();

        DeveloperEntity entity = DataUtils.getJohnDoePersisted();
        BDDMockito.given(developerService.createDeveloper(any(DeveloperEntity.class)))
                .willReturn(Mono.just(entity));
        //when
        WebTestClient.ResponseSpec result = client.post()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(entity.getId())
                .jsonPath("$.firstName").isEqualTo(entity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(entity.getLastName())
                .jsonPath("$.status").isEqualTo(entity.getStatus().name());
    }

    @Test
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDeveloperDtoWithDuplicateEmail_whenCreateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
        BDDMockito.given(developerService.createDeveloper(any(DeveloperEntity.class)))
                .willThrow(new DeveloperWithEmailAlreadyExistsException("Developer with defined email is already exists", "DEVELOPER_DUPLICATE_EMAIL"));
        //when
        WebTestClient.ResponseSpec result = client.post()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isBadRequest()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_DUPLICATE_EMAIL")
                .jsonPath("$.errors[0].message").isEqualTo("Developer with defined email is already exists");
    }

    @Test
    @DisplayName("Test update developer functionality")
    public void givenDeveloperDto_whenUpdateDeveloper_thenSuccessResponse() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();

        DeveloperEntity entity = DataUtils.getJohnDoePersisted();
        BDDMockito.given(developerService.updateDeveloper(any(DeveloperEntity.class)))
                .willReturn(Mono.just(entity));
        //when
        WebTestClient.ResponseSpec result = client.put()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(entity.getId())
                .jsonPath("$.firstName").isEqualTo(entity.getFirstName())
                .jsonPath("$.lastName").isEqualTo(entity.getLastName())
                .jsonPath("$.status").isEqualTo(entity.getStatus().name());
    }

    @Test
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDeveloperDtoWithIncorrectId_whenUpdateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
        BDDMockito.given(developerService.updateDeveloper(any(DeveloperEntity.class)))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = client.put()
                .uri("/api/v1/developers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DeveloperDto.class)
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test
    @DisplayName("Test get all developers functionality")
    public void givenThreeDevelopers_whenGetAllDevelopers_thenDevelopersAreReturned() {
        //given
        DeveloperEntity developer1 = DataUtils.getJohnDoePersisted();
        DeveloperEntity developer2 = DataUtils.getMikeSmithPersisted();
        DeveloperEntity developer3 = DataUtils.getFrankJonesPersisted();

        BDDMockito.given(developerService.getAllDevelopers())
                .willReturn(Flux.just(developer1, developer2, developer3));
        //when
        WebTestClient.ResponseSpec result = client.get()
                .uri("/api/v1/developers")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.size()").isEqualTo(3);
    }

    @Test
    @DisplayName("Test get developer by id functionality")
    public void givenId_whenGetDeveloperById_thenDeveloperIsReturned() {
        //given
        DeveloperEntity developer = DataUtils.getJohnDoePersisted();

        BDDMockito.given(developerService.getDeveloperById(anyInt()))
                .willReturn(Mono.just(developer));
        //when
        WebTestClient.ResponseSpec result = client.get()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(developer.getId())
                .jsonPath("$.firstName").isEqualTo(developer.getFirstName())
                .jsonPath("$.lastName").isEqualTo(developer.getLastName())
                .jsonPath("$.status").isEqualTo(developer.getStatus().name());
    }

    @Test
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetDeveloperById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerService.getDeveloperById(anyInt()))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = client.get()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test
    @DisplayName("Test soft delete developer by id functionality")
    public void givenId_whenSoftDeleteDeveloperById_thenSuccessResponse() {
        //given
        BDDMockito.given(developerService.softDeleteDeveloperById(anyInt()))
                .willReturn(Mono.empty());
        //when
        WebTestClient.ResponseSpec result = client.delete()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test soft delete developer by incorrect id functionality")
    public void givenIncorrectId_whenSoftDeleteDeveloperById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerService.softDeleteDeveloperById(anyInt()))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = client.delete()
                .uri("/api/v1/developers/1")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }

    @Test
    @DisplayName("Test hard delete developer by id functionality")
    public void givenId_whenHardDeleteDeveloperById_thenSuccessResponse() {
        //given
        BDDMockito.given(developerService.hardDeleteDeveloperById(anyInt()))
                .willReturn(Mono.empty());
        //when
        WebTestClient.ResponseSpec result = client.delete()
                .uri("/api/v1/developers/1?isHard=true")
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test hard delete developer by incorrect id functionality")
    public void givenIncorrectId_whenHardDeleteDeveloperById_thenExceptionIsThrown() {
        //given
        BDDMockito.given(developerService.hardDeleteDeveloperById(anyInt()))
                .willThrow(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND"));
        //when
        WebTestClient.ResponseSpec result = client.delete()
                .uri("/api/v1/developers/1?isHard=true")
                .exchange();
        //then
        result.expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.errors[0].code").isEqualTo("DEVELOPER_NOT_FOUND")
                .jsonPath("$.errors[0].message").isEqualTo("Developer not found");
    }
}
