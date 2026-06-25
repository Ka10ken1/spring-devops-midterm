package com.spring_midterm.midterm.service;

import com.spring_midterm.midterm.dto.GridRequest;
import com.spring_midterm.midterm.dto.NoteRequest;
import com.spring_midterm.midterm.dto.NoteResponse;
import com.spring_midterm.midterm.dto.PageResponse;

public interface INoteService {
    NoteResponse create(Long taskId, NoteRequest request);
    NoteResponse getById(Long taskId, Long noteId);
    NoteResponse update(Long taskId, Long noteId, NoteRequest request);
    void delete(Long taskId, Long noteId);
    PageResponse<NoteResponse> getGrid(Long taskId, GridRequest request);
}
