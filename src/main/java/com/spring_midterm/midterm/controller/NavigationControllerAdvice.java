package com.spring_midterm.midterm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class NavigationControllerAdvice {

	@ModelAttribute("isAdmin")
	public boolean isAdmin(Authentication authentication) {
		return authentication != null && authentication.getAuthorities()
				.stream()
				.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
	}
}
