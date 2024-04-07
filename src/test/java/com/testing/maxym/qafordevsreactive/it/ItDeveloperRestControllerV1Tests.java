package com.testing.maxym.qafordevsreactive.it;

import com.testing.maxym.qafordevsreactive.config.PostgreTestcontainerConfig;
import com.testing.maxym.qafordevsreactive.dto.DeveloperDto;
import com.testing.maxym.qafordevsreactive.entity.DeveloperEntity;
import com.testing.maxym.qafordevsreactive.repository.DeveloperRepository;
import com.testing.maxym.qafordevsreactive.util.DataUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import({PostgreTestcontainerConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ItDeveloperRestControllerV1Tests {

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private WebTestClient client;

    @BeforeEach
    public void setUp() {
        developerRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Test create developer functionality")
    public void givenDeveloperDto_whenCreateDeveloper_thenSuccessResponse() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
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
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.lastName").isEqualTo("Doe")
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Test create developer with duplicate email functionality")
    public void givenDeveloperDtoWithDuplicateEmail_whenCreateDeveloper_thenExceptionIsThrown() {
        //given
        String duplicateEmail = "duplicate@gmail.com";
        DeveloperDto dto = DataUtils.getJohnDoeDtoTransient();
        dto.setEmail(duplicateEmail);
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();
        developer.setEmail(duplicateEmail);
        developerRepository.save(developer).block();
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
        String updateEmail = "update@gmail.com";
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();

        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
        dto.setId(entity.getId());
        dto.setEmail(updateEmail);
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
                .jsonPath("$.email").isEqualTo(updateEmail)
                .jsonPath("$.status").isEqualTo(entity.getStatus().name());
    }

    @Test
    @DisplayName("Test update developer with incorrect id functionality")
    public void givenDeveloperDtoWithIncorrectId_whenUpdateDeveloper_thenExceptionIsThrown() {
        //given
        DeveloperDto dto = DataUtils.getJohnDoeDtoPersisted();
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
        DeveloperEntity developer1 = DataUtils.getJohnDoeTransient();
        DeveloperEntity developer2 = DataUtils.getMikeSmithTransient();
        DeveloperEntity developer3 = DataUtils.getFrankJonesTransient();

        developerRepository.saveAll(Flux.just(developer1, developer2, developer3)).blockLast();
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
        DeveloperEntity developer = DataUtils.getJohnDoeTransient();
        developerRepository.save(developer).block();
        //when
        WebTestClient.ResponseSpec result = client.get()
                .uri("/api/v1/developers/" + developer.getId())
                .exchange();
        //then
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println)
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.firstName").isEqualTo(developer.getFirstName())
                .jsonPath("$.lastName").isEqualTo(developer.getLastName())
                .jsonPath("$.status").isEqualTo(developer.getStatus().name());
    }

    @Test
    @DisplayName("Test get developer by incorrect id functionality")
    public void givenIncorrectId_whenGetDeveloperById_thenExceptionIsThrown() {
        //given
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
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = client.delete()
                .uri("/api/v1/developers/" + entity.getId())
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
        DeveloperEntity entity = DataUtils.getJohnDoeTransient();
        developerRepository.save(entity).block();
        //when
        WebTestClient.ResponseSpec result = client.delete()
                .uri("/api/v1/developers/" + entity.getId() + "?isHard=true")
                .exchange();
        //then
        DeveloperEntity obtainedDeveloper = developerRepository.findById(entity.getId()).block();
        assertThat(obtainedDeveloper).isNull();
        result.expectStatus().isOk()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Test hard delete developer by incorrect id functionality")
    public void givenIncorrectId_whenHardDeleteDeveloperById_thenExceptionIsThrown() {
        //given
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
