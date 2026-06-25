package com.spring_midterm.midterm.data;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.spring_midterm.midterm.entity.Student;
import com.spring_midterm.midterm.entity.Task;
import com.spring_midterm.midterm.repository.IRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private IRepository<Student> studentRepository;

    @Mock
    private IRepository<Task> taskRepository;

    @Test
    void shouldSkipIfDataExists() {
        when(studentRepository.count()).thenReturn(5L);

        DataInitializer initializer = new DataInitializer(studentRepository, taskRepository);
        initializer.run();

        verify(studentRepository).count();
        verifyNoInteractions(taskRepository);
    }

    @Test
    void shouldNotFailWhenRunOnCleanDb() {
        when(studentRepository.count()).thenReturn(0L);
        when(studentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DataInitializer initializer = new DataInitializer(studentRepository, taskRepository);
        initializer.run();

        verify(studentRepository, times(2)).save(any(Student.class));
        verify(taskRepository, times(3)).save(any(Task.class));
    }
}
