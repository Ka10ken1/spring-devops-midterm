package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.TaskRequest;
import com.spring_midterm.midterm.dto.TaskResponse;

public interface ITaskService {
    TaskResponse create(Long studentId, TaskRequest request);
    TaskResponse getById(Long studentId, Long taskId);
    TaskResponse update(Long studentId, Long taskId, TaskRequest request);
    void delete(Long studentId, Long taskId);
    PageResponse<TaskResponse> getGrid(Long studentId, GridRequest request);
}
