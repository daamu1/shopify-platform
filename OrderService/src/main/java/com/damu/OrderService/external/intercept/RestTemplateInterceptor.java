package com.damu.OrderService.external.intercept;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;

public class RestTemplateInterceptor implements ClientHttpRequestInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";

    private final OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

    public RestTemplateInterceptor(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
        this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte @NonNull [] body, ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            request.getHeaders().add(CORRELATION_ID_HEADER, correlationId);
        }
        request.getHeaders().add("Authorization",
                "Bearer " +
                oAuth2AuthorizedClientManager
                        .authorize(OAuth2AuthorizeRequest
                                .withClientRegistrationId("internal-client")
                                .principal("internal")
                                .build())
                        .getAccessToken().getTokenValue());

        return execution.execute(request, body);
    }
}
