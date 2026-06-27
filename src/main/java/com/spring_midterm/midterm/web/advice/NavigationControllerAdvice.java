package com.spring_midterm.midterm.web.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class NavigationControllerAdvice {

	@ModelAttribute("isAdmin")
	public boolean isAdmin(Authentication authentication) {
		boolean admin = authentication != null && authentication.getAuthorities()
				.stream()
				.anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
		log.debug("isAdmin check for user: {}", admin);
		return admin;
	}
}
