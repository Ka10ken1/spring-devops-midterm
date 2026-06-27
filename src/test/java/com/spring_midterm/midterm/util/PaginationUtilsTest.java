package com.spring_midterm.midterm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.spring_midterm.midterm.dto.request.GridFilterRequest;
import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.request.SortingRequest;
import com.spring_midterm.midterm.dto.response.PageResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PaginationUtilsTest {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "name", "firstName",
            "firstName", "firstName",
            "lastName", "lastName"
    );

    @Test
    void buildPageable_whenPageSizeNull_shouldReturnUnpaged() {
        var request = new GridRequest(0, null, null, null);
        Pageable result = PaginationUtils.buildPageable(request, SORT_FIELDS);
        assertEquals(Pageable.unpaged().isPaged(), result.isPaged());
    }

    @Test
    void buildPageable_whenPageSizeProvided_shouldReturnPageRequest() {
        var request = new GridRequest(2, 20, null, null);
        Pageable result = PaginationUtils.buildPageable(request, SORT_FIELDS);
        assertInstanceOf(PageRequest.class, result);
        assertEquals(2, result.getPageNumber());
        assertEquals(20, result.getPageSize());
    }

    @Test
    void buildSort_whenSortingNull_shouldDefaultToAscId() {
        var request = new GridRequest(0, 10, null, null);
        Sort result = PaginationUtils.buildSort(request, SORT_FIELDS);
        assertEquals(Sort.by(Sort.Direction.ASC, "id"), result);
    }

    @Test
    void buildSort_whenSortingNameBlank_shouldDefaultToAscId() {
        var request = new GridRequest(0, 10, null, new SortingRequest(1, ""));
        Sort result = PaginationUtils.buildSort(request, SORT_FIELDS);
        assertEquals(Sort.by(Sort.Direction.ASC, "id"), result);
    }

    @Test
    void buildSort_whenDirectionNull_shouldDefaultToAsc() {
        var request = new GridRequest(0, 10, null, new SortingRequest(null, "firstName"));
        Sort result = PaginationUtils.buildSort(request, SORT_FIELDS);
        assertEquals(Sort.by(Sort.Direction.ASC, "firstName"), result);
    }

    @Test
    void buildSort_whenDirectionAsc_shouldReturnAsc() {
        var request = new GridRequest(0, 10, null, new SortingRequest(1, "firstName"));
        Sort result = PaginationUtils.buildSort(request, SORT_FIELDS);
        assertEquals(Sort.by(Sort.Direction.ASC, "firstName"), result);
    }

    @Test
    void buildSort_whenDirectionDesc_shouldReturnDesc() {
        var request = new GridRequest(0, 10, null, new SortingRequest(-1, "firstName"));
        Sort result = PaginationUtils.buildSort(request, SORT_FIELDS);
        assertEquals(Sort.by(Sort.Direction.DESC, "firstName"), result);
    }

    @Test
    void buildSort_whenSortFieldMapped_shouldUseMappedField() {
        var request = new GridRequest(0, 10, null, new SortingRequest(1, "name"));
        Sort result = PaginationUtils.buildSort(request, SORT_FIELDS);
        assertEquals(Sort.by(Sort.Direction.ASC, "firstName"), result);
    }

    @Test
    void buildSort_whenSortFieldUnmapped_shouldFallbackToId() {
        var request = new GridRequest(0, 10, null, new SortingRequest(1, "unknownField"));
        Sort result = PaginationUtils.buildSort(request, SORT_FIELDS);
        assertEquals(Sort.by(Sort.Direction.ASC, "id"), result);
    }

    @Test
    void toPageResponse_shouldMapAllFields() {
        Page<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 10);
        List<String> items = List.of("a", "b");

        PageResponse<String> response = PaginationUtils.toPageResponse(page, items);

        assertEquals(List.of("a", "b"), response.data());
        assertEquals(1, response.pageIndex());
        assertEquals(2, response.pageSize());
        assertEquals(10, response.totalElements());
        assertEquals(5, response.totalPages());
    }
}
