package com.spring_midterm.midterm.util;

import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.response.PageResponse;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public final class PaginationUtils {

    private PaginationUtils() {}

    public static Pageable buildPageable(GridRequest request, Map<String, String> sortFields) {
        Sort sort = buildSort(request, sortFields);
        if (request.pageSize() == null) {
            return Pageable.unpaged(sort);
        }
        return PageRequest.of(request.pageIndex(), request.pageSize(), sort);
    }

    public static Sort buildSort(GridRequest request, Map<String, String> sortFields) {
        if (request.sorting() == null || !StringUtils.hasText(request.sorting().sortingname())) {
            return Sort.by(Sort.Direction.ASC, "id");
        }
        String sortField = sortFields.getOrDefault(request.sorting().sortingname(), "id");
        Sort.Direction direction = request.sorting().direction() != null && request.sorting().direction() == -1
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, sortField);
    }

    public static <T> PageResponse<T> toPageResponse(Page<?> page, List<T> items) {
        return new PageResponse<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
