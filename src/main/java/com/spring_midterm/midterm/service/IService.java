package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;

public interface IService<TRequest, TResponse> {

	TResponse create(TRequest request);

	PageResponse<TResponse> getGrid(GridRequest request);

	TResponse getById(Long id);

	TResponse update(Long id, TRequest request);

	void delete(Long id);
}
