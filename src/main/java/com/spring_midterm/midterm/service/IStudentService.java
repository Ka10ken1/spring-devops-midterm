package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.request.GridRequest;
import com.spring_midterm.midterm.dto.response.PageResponse;
import com.spring_midterm.midterm.dto.request.StudentRequest;
import com.spring_midterm.midterm.dto.response.StudentResponse;

public interface IStudentService {
    StudentResponse create(StudentRequest request);
    StudentResponse getById(Long id);
    StudentResponse update(Long id, StudentRequest request);
    void delete(Long id);
    PageResponse<StudentResponse> getGrid(GridRequest request);
}
