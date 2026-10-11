package com.demandworkbench.decomposition.application;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class InterruptedTaskRecovery {
    private final DecompositionRepository repository;

    public InterruptedTaskRecovery(DecompositionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void recover() {
        repository.failInterruptedTasks();
    }
}
