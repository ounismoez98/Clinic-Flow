package com.example.mspharmacie.ai;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

@Configuration
public class GeminiClientConfig {

	@Bean(name = "geminiWebClient")
	public WebClient geminiWebClient(GeminiProperties props, ObjectMapper objectMapper) {
		HttpClient reactorHttp = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
			.responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()));

		var strategies = ExchangeStrategies.builder().codecs(c -> {
			c.customCodecs().register(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
			c.customCodecs().register(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
			c.defaultCodecs().maxInMemorySize(4 * 1024 * 1024);
		}).build();

		return WebClient.builder()
			.baseUrl(props.getBaseUrl())
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
			.exchangeStrategies(strategies)
			.clientConnector(new ReactorClientHttpConnector(reactorHttp))
			.build();
	}
}
