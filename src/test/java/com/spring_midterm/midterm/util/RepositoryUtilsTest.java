package com.spring_midterm.midterm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.spring_midterm.midterm.exception.ResourceNotFoundException;
import com.spring_midterm.midterm.repository.IRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RepositoryUtilsTest {

    @Mock
    private IRepository<String> repository;

    @Test
    void findOrThrow_whenFound_shouldReturnEntity() {
        when(repository.findById(1L)).thenReturn(Optional.of("entity"));

        String result = RepositoryUtils.findOrThrow(repository, 1L, "test");

        assertEquals("entity", result);
    }

    @Test
    void findOrThrow_whenNotFound_shouldThrow() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                RepositoryUtils.findOrThrow(repository, 99L, "test")
        );
    }
}
