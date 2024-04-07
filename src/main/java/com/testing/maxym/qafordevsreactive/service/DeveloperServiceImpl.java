package com.testing.maxym.qafordevsreactive.service;

import com.testing.maxym.qafordevsreactive.entity.DeveloperEntity;
import com.testing.maxym.qafordevsreactive.entity.Status;
import com.testing.maxym.qafordevsreactive.exception.DeveloperNotFoundException;
import com.testing.maxym.qafordevsreactive.exception.DeveloperWithEmailAlreadyExistsException;
import com.testing.maxym.qafordevsreactive.repository.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;

    private Mono<Void> checkIfExistsByEmail(String email) {
        return developerRepository.findByEmail(email)
                .flatMap(developer -> {
                    if (nonNull(developer))
                        return Mono.error(new DeveloperWithEmailAlreadyExistsException("Developer with defined email is already exists", "DEVELOPER_DUPLICATE_EMAIL"));
                    return Mono.empty();
                });
    }

    @Override
    public Mono<DeveloperEntity> createDeveloper(DeveloperEntity developer) {
        return checkIfExistsByEmail(developer.getEmail())
                .then(Mono.defer(() -> {
                    developer.setStatus(Status.ACTIVE);
                    return developerRepository.save(developer);
                }));
    }

    @Override
    public Mono<DeveloperEntity> updateDeveloper(DeveloperEntity developer) {
        return developerRepository.findById(developer.getId())
                .switchIfEmpty(Mono.error(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND")))
                .flatMap(d -> developerRepository.save(developer));
    }

    @Override
    public Flux<DeveloperEntity> getAllDevelopers() {
        return developerRepository.findAll();
    }

    @Override
    public Flux<DeveloperEntity> getAllActiveBySpecialty(String specialty) {
        return developerRepository.findAllActiveBySpecialty(specialty);
    }

    @Override
    public Mono<DeveloperEntity> getDeveloperById(Integer id) {
        return developerRepository.findById(id);
    }

    @Override
    public Mono<Void> softDeleteDeveloperById(Integer id) {
        return getDeveloperById(id)
                .switchIfEmpty(Mono.error(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND")))
                .flatMap(developer -> {
                        developer.setStatus(Status.DELETED);
                        return developerRepository.save(developer).then();
                });
    }

    @Override
    public Mono<Void> hardDeleteDeveloperById(Integer id) {
        return getDeveloperById(id)
                .switchIfEmpty(Mono.error(new DeveloperNotFoundException("Developer not found", "DEVELOPER_NOT_FOUND")))
                .flatMap(developer -> developerRepository.deleteById(id).then());
    }
}
