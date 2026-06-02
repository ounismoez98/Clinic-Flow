package com.example.mspharmacie.web;

public class GeminiNotConfiguredException extends RuntimeException {

	public GeminiNotConfiguredException() {
		super("Configuration Gemini absente : définir la variable d'environnement GEMINI_API_KEY ou la propriété gemini.api-key.");
	}
}
