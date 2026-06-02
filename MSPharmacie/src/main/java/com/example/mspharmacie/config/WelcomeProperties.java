package com.example.mspharmacie.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Binds {@code welcome.message} from Config Server. {@link RefreshScope} recreates this bean on {@code POST /actuator/refresh}.
 */
@Component
@RefreshScope
@ConfigurationProperties(prefix = "welcome")
public class WelcomeProperties {

	private String message = "";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
