package com.example.mspharmacie.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.example.mspharmacie.ai.GeminiSummaryService;
import com.example.mspharmacie.IMedicamentService;
import com.example.mspharmacie.dto.MedicamentAssistantSummaryDto;
import com.example.mspharmacie.dto.MedicamentResponseDto;

/** Verifies the REST path routed by the API gateway ({@code GET /medicaments/{id}/assistant-summary}). */
@ExtendWith(MockitoExtension.class)
class MedicamentAssistantSummaryControllerTest {

	private MockMvc mockMvc;

	@Mock
	private IMedicamentService medicamentService;

	@Mock
	private GeminiSummaryService geminiSummaryService;

	@BeforeEach
	void setUp() {
		MedicamentRestController controller = new MedicamentRestController(medicamentService, geminiSummaryService);
		mockMvc = standaloneSetup(controller).setControllerAdvice(new RestExceptionHandler()).build();
	}

	@Test
	void assistantSummary_returnsJson() throws Exception {
		MedicamentResponseDto catalog = new MedicamentResponseDto(1, "Paracétamol 500mg", true);
		when(medicamentService.findCatalogById(1)).thenReturn(catalog);
		when(geminiSummaryService.summarizeMedicamentCatalog(any(MedicamentResponseDto.class)))
			.thenReturn(new MedicamentAssistantSummaryDto(1, "Paracétamol 500mg", "Résumé général.",
					GeminiSummaryService.FIXED_DISCLAIMER));

		mockMvc.perform(get("/medicaments/1/assistant-summary"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.medicamentId").value(1))
			.andExpect(jsonPath("$.nomMedicament").value("Paracétamol 500mg"))
			.andExpect(jsonPath("$.summary").value("Résumé général."))
			.andExpect(jsonPath("$.disclaimer").exists());
	}

	@Test
	void assistantSummary_serviceUnavailable_whenGeminiNotConfigured() throws Exception {
		MedicamentResponseDto catalog = new MedicamentResponseDto(1, "X", true);
		when(medicamentService.findCatalogById(1)).thenReturn(catalog);
		when(geminiSummaryService.summarizeMedicamentCatalog(any(MedicamentResponseDto.class)))
			.thenThrow(new GeminiNotConfiguredException());

		mockMvc.perform(get("/medicaments/1/assistant-summary")).andExpect(status().isServiceUnavailable());
	}
}
