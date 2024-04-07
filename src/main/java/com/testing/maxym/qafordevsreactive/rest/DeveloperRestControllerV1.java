package com.testing.maxym.qafordevsreactive.rest;

import com.testing.maxym.qafordevsreactive.dto.DeveloperDto;
import com.testing.maxym.qafordevsreactive.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/developers")
@RequiredArgsConstructor
public class DeveloperRestControllerV1 {

    private final DeveloperService developerService;

    @PostMapping
    public Mono<?> createDeveloper(@RequestBody DeveloperDto developerDto) {
        return developerService.createDeveloper(developerDto.toEntity())
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @PutMapping
    public Mono<?> updateDeveloper(@RequestBody DeveloperDto developerDto) {
        return developerService.updateDeveloper(developerDto.toEntity())
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @GetMapping
    public Flux<?> getAllDevelopers() {
        return developerService.getAllDevelopers()
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @GetMapping("/specialty/{specialty}")
    public Flux<?> getAllDevelopersBySpecialty(@PathVariable("specialty") String speciality) {
        return developerService.getAllActiveBySpecialty(speciality)
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @GetMapping("/{id}")
    public Mono<?> getDeveloperById(@PathVariable("id") Integer id) {
        return developerService.getDeveloperById(id)
                .flatMap(entity -> Mono.just(DeveloperDto.fromEntity(entity)));
    }

    @DeleteMapping("/{id}")
    public Mono<?> deleteDeveloperById(@PathVariable("id") Integer id,
                                       @RequestParam(value = "isHard", defaultValue = "false") boolean isHard) {
        if (isHard) {
            return developerService.hardDeleteDeveloperById(id);
        }
        return developerService.softDeleteDeveloperById(id);
    }
}
