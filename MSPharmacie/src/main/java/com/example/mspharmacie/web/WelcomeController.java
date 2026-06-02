package com.example.mspharmacie.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mspharmacie.config.WelcomeProperties;
import com.example.mspharmacie.dto.WelcomeResponse;

@RestController
@RequestMapping("/welcome-message")
public class WelcomeController {

	private final WelcomeProperties welcomeProperties;

	public WelcomeController(WelcomeProperties welcomeProperties) {
		this.welcomeProperties = welcomeProperties;
	}

	@GetMapping
	public WelcomeResponse welcome() {
		return new WelcomeResponse(welcomeProperties.getMessage());
	}
}
