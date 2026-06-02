package com.example.mspharmacie.web;

public class GeminiUpstreamException extends RuntimeException {

	public GeminiUpstreamException() {
		super("Le service d'assistant est temporairement indisponible.");
	}

	public GeminiUpstreamException(String message) {
		super(message != null ? message : "Le service d'assistant est temporairement indisponible.");
	}

	public GeminiUpstreamException(String message, Throwable cause) {
		super(message != null ? message : "Le service d'assistant est temporairement indisponible.", cause);
	}

	public GeminiUpstreamException(Throwable cause) {
		super("Le service d'assistant est temporairement indisponible.", cause);
	}
}
