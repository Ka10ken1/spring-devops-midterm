package com.spring_midterm.midterm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

	private final String resource;
	private final String identifier;

	public ResourceNotFoundException(String resource, Long id) {
		this(resource, String.valueOf(id));
	}

	public ResourceNotFoundException(String resource, String identifier) {
		super(resource + " with id " + identifier + " was not found");
		this.resource = resource;
		this.identifier = identifier;
	}

	public String getResource() {
		return resource;
	}

	public String getIdentifier() {
		return identifier;
	}
}
