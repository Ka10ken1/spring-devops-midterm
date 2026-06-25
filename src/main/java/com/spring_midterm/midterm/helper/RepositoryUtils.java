package com.spring_midterm.midterm.helper;

import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.repository.IRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RepositoryUtils {

    private RepositoryUtils() {}

    public static <T> T findOrThrow(IRepository<T> repository, Long id, String resourceName) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("{} with id {} not found", resourceName, id);
                    return new ResourceNotFoundException(resourceName, id);
                });
    }
}
