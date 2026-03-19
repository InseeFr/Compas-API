package fr.insee.compas.client.configuration.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import feign.Feign;
import feign.Feign.Builder;
import feign.RequestInterceptor;
import okhttp3.OkHttpClient;

public class CustomFeignAuthentification {
    @Value("${compas.web.springdoc.token-url:#{'localhost:8080'}}")
    private String tokenUri;

    @Value("${compas.service.keycloak.client}")
    private String clientId;

    @Value("${compas.service.keycloak.credentials.secret}")
    private String clientSecret;

    private static final Authentication ANONYMOUS_PRINCIPAL =
            new AnonymousAuthenticationToken(
                    "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

    @Bean
    Builder feignBuilder() {
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        return Feign.builder().client(new feign.okhttp.OkHttpClient(okHttpClient));
    }

    @Bean
    @ConditionalOnProperty(
            name = "oscar.client.authType",
            havingValue = "keycloak",
            matchIfMissing = true)
    public RequestInterceptor requestInterceptor(
            OAuth2AuthorizedClientManager authorizedClientManager) {

        return requestTemplate -> {
            final OAuth2AuthorizeRequest authorizeRequest =
                    OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                            .principal(ANONYMOUS_PRINCIPAL)
                            .build();
            final OAuth2AuthorizedClient authorizedClient =
                    authorizedClientManager.authorize(authorizeRequest);
            final OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
            requestTemplate.header(
                    HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getTokenValue());
        };
    }

    // Si on utilisait que dans un contexte web, on pourrait se débarasser de tous les bean ci-après
    // et laisser SpringBoot les créer automatiquement à partir
    // des bonnes properties, mais on l'utilise dans un contexte batch donc on doit créer tous ces
    // beans nous même
    @Bean
    @ConditionalOnProperty(
            name = "oscar.client.authType",
            havingValue = "keycloak",
            matchIfMissing = true)
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService clientService) {

        final OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();

        final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, clientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    @ConditionalOnProperty(
            name = "oscar.client.authType",
            havingValue = "keycloak",
            matchIfMissing = true)
    public ClientRegistrationRepository clientRegistrationRepository() {
        final ClientRegistration sirene4ClientRegistration =
                ClientRegistration.withRegistrationId("keycloak")
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .tokenUri(tokenUri)
                        .clientName("oscar4-service")
                        .build();
        return new InMemoryClientRegistrationRepository(sirene4ClientRegistration);
    }

    @Bean
    @ConditionalOnProperty(
            name = "oscar.client.authType",
            havingValue = "keycloak",
            matchIfMissing = true)
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    @ConditionalOnProperty(
            name = "oscar.client.authType",
            havingValue = "keycloak",
            matchIfMissing = true)
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }
}
