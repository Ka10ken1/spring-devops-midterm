package com.spring_midterm.midterm.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/login", "/css/**", "/health", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/info").permitAll()
						.requestMatchers("/actuator/**").hasRole("ADMIN")
						.requestMatchers("/manage/health", "/manage/info").permitAll()
						.requestMatchers("/manage/prometheus").permitAll()
						.requestMatchers("/manage/**").hasRole("ADMIN")
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/profile", "/students/**", "/tasks/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/students").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/students/*").hasRole("ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/api/students/*").hasRole("ADMIN")
						.requestMatchers("/api/**").authenticated()
						.anyRequest().authenticated()
				)
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/profile", true)
						.permitAll()
				)
				.logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
				.exceptionHandling(exception -> exception.accessDeniedHandler((request, response, accessDeniedException) -> {
					if (request.getRequestURI().startsWith("/api/")) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN);
						return;
					}
					response.sendRedirect("/access-denied");
				}))
				.httpBasic(Customizer.withDefaults())
				.build();
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		UserDetails user = User.builder()
				.username("user")
				.password(passwordEncoder.encode("user123"))
				.roles("USER")
				.build();

		UserDetails admin = User.builder()
				.username("admin")
				.password(passwordEncoder.encode("admin123"))
				.roles("ADMIN", "USER")
				.build();

		return new InMemoryUserDetailsManager(user, admin);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
