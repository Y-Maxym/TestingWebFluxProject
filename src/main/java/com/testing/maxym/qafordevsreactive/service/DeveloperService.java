package com.testing.maxym.qafordevsreactive.service;

import com.testing.maxym.qafordevsreactive.entity.DeveloperEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeveloperService {

    Mono<DeveloperEntity> createDeveloper(DeveloperEntity developer);

    Mono<DeveloperEntity> updateDeveloper(DeveloperEntity developer);

    Flux<DeveloperEntity> getAllDevelopers();

    Flux<DeveloperEntity> getAllActiveBySpecialty(String specialty);

    Mono<DeveloperEntity> getDeveloperById(Integer id);

    Mono<Void> softDeleteDeveloperById(Integer id);

    Mono<Void> hardDeleteDeveloperById(Integer id);
}
