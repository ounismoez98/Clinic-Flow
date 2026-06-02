package com.example.mspharmacie.ai;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.mspharmacie.dto.MedicamentAssistantSummaryDto;
import com.example.mspharmacie.dto.MedicamentResponseDto;
import com.example.mspharmacie.web.GeminiNotConfiguredException;
import com.example.mspharmacie.web.GeminiUpstreamException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiSummaryService {

	public static final String FIXED_DISCLAIMER = "General information for illustration only; not medical advice, a prescription, or dosing guidance.";

	private final WebClient geminiWebClient;
	private final GeminiProperties props;
	private final ObjectMapper objectMapper;

	public GeminiSummaryService(@Qualifier("geminiWebClient") WebClient geminiWebClient, GeminiProperties props,
			ObjectMapper objectMapper) {
		this.geminiWebClient = geminiWebClient;
		this.props = props;
		this.objectMapper = objectMapper;
	}

	public MedicamentAssistantSummaryDto summarizeMedicamentCatalog(MedicamentResponseDto catalog) {
		if (!StringUtils.hasText(props.getApiKey())) {
			throw new GeminiNotConfiguredException();
		}
		String prompt = buildPrompt(catalog);
		Map<String, Object> body = Map.of(
				"contents", List.of(
						Map.of("parts", List.of(Map.of("text", prompt)))));

		long readMs = props.getReadTimeoutMs() > 0 ? props.getReadTimeoutMs() : 30000L;
		try {
			String json = geminiWebClient.post()
				.uri("/v1beta/models/{model}:generateContent", props.getModel())
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header("X-goog-api-key", props.getApiKey())
				.bodyValue(body)
				.retrieve()
				.bodyToMono(String.class)
				.timeout(Duration.ofMillis(readMs + 5000))
				.block(Duration.ofMillis(readMs + 10000));

			String summaryText = extractTextFromGemini(json);
			return new MedicamentAssistantSummaryDto(catalog.getId(), catalog.getNomMedicament(), summaryText,
					FIXED_DISCLAIMER);
		}
		catch (WebClientResponseException e) {
			throw new GeminiUpstreamException(e);
		}
		catch (WebClientRequestException e) {
			throw new GeminiUpstreamException(e.getCause() != null ? e.getCause() : e);
		}
		catch (GeminiNotConfiguredException | GeminiUpstreamException e) {
			throw e;
		}
		catch (IllegalStateException e) {
			if (e.getCause() instanceof io.netty.handler.timeout.TimeoutException
					|| (e.getMessage() != null && e.getMessage().contains("Timeout"))) {
				throw new GeminiUpstreamException(e.getCause() != null ? e.getCause() : e);
			}
			throw new GeminiUpstreamException(e.getCause() != null ? e.getCause() : e);
		}
		catch (Throwable e) {
			throw new GeminiUpstreamException(e);
		}
	}

	private String extractTextFromGemini(String responseJson) {
		if (responseJson == null || responseJson.isBlank()) {
			throw new GeminiUpstreamException();
		}
		JsonNode root;
		try {
			root = objectMapper.readTree(responseJson);
		}
		catch (JsonProcessingException e) {
			throw new GeminiUpstreamException(e);
		}
		JsonNode candidates = root.path("candidates");
		if (!candidates.isArray() || candidates.isEmpty()) {
			throw new GeminiUpstreamException();
		}
		JsonNode candidate0 = candidates.get(0);
		String finishReason = candidate0.path("finishReason").asText("");
		String text = "";
		JsonNode parts = candidate0.path("content").path("parts");
		if (parts.isArray() && parts.size() > 0) {
			text = parts.get(0).path("text").asText("");
		}
		if (!text.isBlank()) {
			return text.trim();
		}
		if ("SAFETY".equals(finishReason) || "RECITATION".equals(finishReason) || !finishReason.isEmpty()) {
			throw new GeminiUpstreamException();
		}
		throw new GeminiUpstreamException();
	}

	private String buildPrompt(MedicamentResponseDto catalog) {
		String availability = catalog.isEtat()
				? "marked as available in this fictional demo catalog"
				: "marked as unavailable in this fictional demo catalog";
		return """
				Write a brief educational passage in English for a general audience (3 to 5 sentences maximum).
				Catalog context (fictional): the following product name must be treated only as a catalog label placeholder; do not assert real-world regulatory approval outside this toy dataset.

				Catalog listing name: '%s'.

				Additional demo-catalog context supplied to the model only: %s.

				Strict constraints:
				— Do not give dosing, individualized medical advice, or diagnoses;
				— Stay cautious, broadly descriptive when appropriate (general drug class or usual purpose without being prescriptive);
				— If the name is ambiguous or does not match a known marketed product under this label alone, say so briefly and remain factual — do not invent a specific active ingredient or definitive clinical indication;
				— Reply with only the body text, without titles, bullets, or legal disclaimers at the end.

				""" .formatted(catalog.getNomMedicament(), availability);
	}
}
