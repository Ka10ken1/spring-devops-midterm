package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.PageResponse;
import com.spring_midterm.midterm.dto.StudentRequest;
import com.spring_midterm.midterm.dto.StudentResponse;

public interface IStudentService {
    StudentResponse create(StudentRequest request);
    StudentResponse getById(Long id);
    StudentResponse update(Long id, StudentRequest request);
    void delete(Long id);
    PageResponse<StudentResponse> getGrid(GridRequest request);
}
