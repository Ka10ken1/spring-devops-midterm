package com.spring_midterm.midterm.dto.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "A"})
    void studentRequest_firstName_invalid(String firstName) {
        var request = new StudentRequest(firstName, "Doe", "john@example.com");
        Set<ConstraintViolation<StudentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"Alice,true", "A,false", ",false", "' ',false"})
    void studentRequest_firstName_valid(String firstName, boolean expectValid) {
        var request = new StudentRequest(firstName, "Doe", "john@example.com");
        Set<ConstraintViolation<StudentRequest>> violations = validator.validate(request);
        assertEquals(expectValid, violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "B"})
    void studentRequest_lastName_invalid(String lastName) {
        var request = new StudentRequest("John", lastName, "john@example.com");
        Set<ConstraintViolation<StudentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"not-an-email", " "})
    void studentRequest_email_invalid(String email) {
        var request = new StudentRequest("John", "Doe", email);
        Set<ConstraintViolation<StudentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @CsvSource({"john@example.com,true", "'',false", "not-email,false"})
    void studentRequest_email_valid(String email, boolean expectValid) {
        var request = new StudentRequest("John", "Doe", email);
        Set<ConstraintViolation<StudentRequest>> violations = validator.validate(request);
        assertEquals(expectValid, violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "X"})
    void taskRequest_title_invalid(String title) {
        var request = new TaskRequest(title, "desc", false, null);
        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Valid Title"})
    void taskRequest_title_valid(String title) {
        var request = new TaskRequest(title, "desc", false, null);
        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "C"})
    void noteRequest_content_invalid(String content) {
        var request = new NoteRequest(content);
        Set<ConstraintViolation<NoteRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Valid note content"})
    void noteRequest_content_valid(String content) {
        var request = new NoteRequest(content);
        Set<ConstraintViolation<NoteRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100})
    void gridRequest_pageIndex_invalid(int pageIndex) {
        var request = new GridRequest(pageIndex, 10, null, null);
        Set<ConstraintViolation<GridRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100})
    void gridRequest_pageIndex_valid(int pageIndex) {
        var request = new GridRequest(pageIndex, 10, null, null);
        Set<ConstraintViolation<GridRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void gridRequest_pageSize_invalid(int pageSize) {
        var request = new GridRequest(0, pageSize, null, null);
        Set<ConstraintViolation<GridRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
