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
class OwnershipValidatorTest {

    @Mock
    private IRepository<Object> parentRepo;

    @Mock
    private IRepository<Object> childRepo;

    @Test
    void validate_whenParentIdMatches_shouldReturnChild() {
        Object parent = new Object();
        Object child = new Object();
        when(parentRepo.findById(1L)).thenReturn(Optional.of(parent));
        when(childRepo.findById(10L)).thenReturn(Optional.of(child));

        Object result = OwnershipValidator.validate(
                parentRepo, "parent", 1L,
                childRepo, "child", 10L,
                c -> 1L
        );

        assertEquals(child, result);
    }

    @Test
    void validate_whenParentIdMismatch_shouldThrow() {
        Object parent = new Object();
        Object child = new Object();
        when(parentRepo.findById(1L)).thenReturn(Optional.of(parent));
        when(childRepo.findById(10L)).thenReturn(Optional.of(child));

        assertThrows(ResourceNotFoundException.class, () ->
                OwnershipValidator.validate(
                        parentRepo, "parent", 1L,
                        childRepo, "child", 10L,
                        c -> 99L
                )
        );
    }
}
