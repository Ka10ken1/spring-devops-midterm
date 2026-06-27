package com.spring_midterm.midterm.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.settings")
@Validated
public class AppSettings {

	@NotBlank
	private String title;

	@Min(1)
	private int paginationLimit;

	@NotBlank
	@Email
	private String contactEmail;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getPaginationLimit() {
		return paginationLimit;
	}

	public void setPaginationLimit(int paginationLimit) {
		this.paginationLimit = paginationLimit;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}
}
