package com.spring_midterm.midterm.helper;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import org.springframework.data.jpa.domain.Specification;

public final class SpecificationHelper {

    private SpecificationHelper() {}

    @SafeVarargs
    public static <T> Specification<T> anyLikeIgnoreCase(String value, String... fields) {
        if (value == null || value.isBlank()) return (root, query, cb) -> cb.conjunction();
        String pattern = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
            Arrays.stream(fields)
                .map(f -> cb.like(cb.lower(root.get(f)), pattern))
                .toArray(Predicate[]::new)
        );
    }

    public static <T> Specification<T> likeIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) return (root, query, cb) -> cb.conjunction();
        String pattern = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), pattern);
    }

    public static <T> Specification<T> equal(String field, Object value) {
        if (value == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> equalPath(String path, Object value) {
        if (value == null) return (root, query, cb) -> cb.conjunction();
        String[] parts = path.split("\\.");
        return (root, query, cb) -> {
            Path<?> expr = root.get(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                expr = expr.get(parts[i]);
            }
            return cb.equal(expr, value);
        };
    }
}
