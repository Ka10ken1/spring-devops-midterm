package com.spring_midterm.midterm.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SpecificationHelperTest {

    @Mock
    private Root<Object> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        lenient().when(cb.conjunction()).thenReturn(predicate);
    }

    @Test
    void anyLikeIgnoreCase_whenNullValue_shouldReturnConjunction() {
        Specification<Object> spec = SpecificationHelper.anyLikeIgnoreCase(null, "field1");
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).conjunction();
    }

    @Test
    void anyLikeIgnoreCase_whenBlankValue_shouldReturnConjunction() {
        Specification<Object> spec = SpecificationHelper.anyLikeIgnoreCase("  ", "field1");
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).conjunction();
    }

    @Test
    void anyLikeIgnoreCase_whenValidValue_shouldReturnOrPredicate() {
        when(cb.lower(root.get("field1"))).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(cb.lower(root.get("field2"))).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(cb.like(any(), anyString())).thenReturn(predicate);
        when(cb.or(new Predicate[]{predicate, predicate})).thenReturn(predicate);

        Specification<Object> spec = SpecificationHelper.anyLikeIgnoreCase("test", "field1", "field2");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).or(new Predicate[]{predicate, predicate});
    }

    @Test
    void likeIgnoreCase_whenNullValue_shouldReturnConjunction() {
        Specification<Object> spec = SpecificationHelper.likeIgnoreCase("field1", null);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).conjunction();
    }

    @Test
    void likeIgnoreCase_whenBlankValue_shouldReturnConjunction() {
        Specification<Object> spec = SpecificationHelper.likeIgnoreCase("field1", "");
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).conjunction();
    }

    @Test
    void likeIgnoreCase_whenValidValue_shouldReturnLikePredicate() {
        when(cb.lower(root.get("field1"))).thenReturn(mock(jakarta.persistence.criteria.Expression.class));
        when(cb.like(any(), anyString())).thenReturn(predicate);

        Specification<Object> spec = SpecificationHelper.likeIgnoreCase("field1", "test");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).like(any(), anyString());
    }

    @Test
    void equal_whenNullValue_shouldReturnConjunction() {
        Specification<Object> spec = SpecificationHelper.equal("field1", null);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).conjunction();
    }

    @Test
    void equal_whenValidValue_shouldReturnEqualPredicate() {
        when(root.get("field1")).thenReturn(mock(Path.class));
        when(cb.equal(any(), anyString())).thenReturn(predicate);

        Specification<Object> spec = SpecificationHelper.equal("field1", "test");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(any(), anyString());
    }

    @Test
    void equalPath_whenNullValue_shouldReturnConjunction() {
        Specification<Object> spec = SpecificationHelper.equalPath("path.field", null);
        Predicate result = spec.toPredicate(root, query, cb);
        assertNotNull(result);
        verify(cb).conjunction();
    }

    @Test
    void equalPath_whenSinglePart_shouldReturnEqualPredicate() {
        Path<Object> fieldPath = mock(Path.class);
        when(root.get("field1")).thenReturn(fieldPath);
        when(cb.equal(fieldPath, "test")).thenReturn(predicate);

        Specification<Object> spec = SpecificationHelper.equalPath("field1", "test");
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(cb).equal(fieldPath, "test");
    }

    @Test
    void equalPath_whenMultiPart_shouldTraversePath() {
        Path<Object> studentPath = mock(Path.class);
        Path<Object> idPath = mock(Path.class);
        when(root.get("student")).thenReturn(studentPath);
        when(studentPath.get("id")).thenReturn(idPath);
        when(cb.equal(idPath, 1L)).thenReturn(predicate);

        Specification<Object> spec = SpecificationHelper.equalPath("student.id", 1L);
        Predicate result = spec.toPredicate(root, query, cb);

        assertNotNull(result);
        verify(root).get("student");
        verify(studentPath).get("id");
        verify(cb).equal(idPath, 1L);
    }
}
