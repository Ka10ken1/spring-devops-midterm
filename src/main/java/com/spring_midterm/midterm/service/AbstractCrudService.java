package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.repository.IRepository;
import com.spring_midterm.midterm.util.PaginationUtils;
import com.spring_midterm.midterm.util.RepositoryUtils;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public abstract class AbstractCrudService<T, TReq, TRes> {

    protected final IRepository<T> repository;

    protected AbstractCrudService(IRepository<T> repository) {
        this.repository = repository;
    }

    protected T findOrThrow(Long id) {
        return RepositoryUtils.findOrThrow(repository, id, getResourceName());
    }

    protected TRes saveAndRespond(T entity) {
        return toResponse(repository.save(entity));
    }

    protected PageResponse<TRes> findGrid(Specification<T> spec, GridRequest request) {
        Pageable pageable = PaginationUtils.buildPageable(request, getSortFields());
        Page<T> page = repository.findAll(spec, pageable);
        List<TRes> items = page.getContent().stream().map(this::toResponse).toList();
        return PaginationUtils.toPageResponse(page, items);
    }

    protected abstract T createEntity();
    protected abstract TRes toResponse(T entity);
    protected abstract Map<String, String> getSortFields();
    protected abstract String getResourceName();

    protected Specification<T> buildSpecification(GridRequest request) {
        return (root, query, cb) -> cb.conjunction();
    }
}
