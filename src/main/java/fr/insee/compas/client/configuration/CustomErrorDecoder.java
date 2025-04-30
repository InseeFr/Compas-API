package fr.insee.compas.client.configuration;

import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.compas.exception.CompasClientException;
import fr.insee.compas.exception.CompasUploadException;
import fr.insee.compas.exception.ErrorVM;

import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomErrorDecoder {

    private static final String MESSAGE_ERROR_UNAUTHORIZED_CLE = "api.compas.unauthorized";
    private static final String MESSAGE_ERROR_UNAUTHORIZED_MESSAGE =
            "Problème d'authentification depuis l'API compas";

    private static final String MESSAGE_ERROR_FORBIDDEN_CLE = "api.compas.forbidden";
    private static final String MESSAGE_ERROR_FORBIDDEN_MESSAGE =
            "Problème d'autorisation depuis l'API compas";

    @Bean
    public feign.Logger.Level feignLoggerLevel() {
        return feign.Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder demandeFeignErrorDecoder() {
        final ErrorDecoder decoder = new ErrorDecoder.Default();
        return (methodKey, response) -> {
            log.error("Erreur rencontrée dans appel {} - Status {}", methodKey, response.status());
            ErrorVM errorVM = null;
            String body = null;
            try {
                body = IOUtils.toString(response.body().asReader(StandardCharsets.UTF_8));
                errorVM = new ObjectMapper().readValue(body, ErrorVM.class);
            } catch (final Exception e) {
                log.debug(" exception relevée", e);
                errorVM = new ErrorVM();
                if (HttpStatus.UNAUTHORIZED.value() == response.status()) {
                    errorVM.setCle(MESSAGE_ERROR_UNAUTHORIZED_CLE);
                    errorVM.setMessage(MESSAGE_ERROR_UNAUTHORIZED_MESSAGE);
                } else if (HttpStatus.FORBIDDEN.value() == response.status()) {
                    errorVM.setCle(MESSAGE_ERROR_FORBIDDEN_CLE);
                    errorVM.setMessage(MESSAGE_ERROR_FORBIDDEN_MESSAGE);
                } else {
                    errorVM.setCle(body);
                    errorVM.setMessage(body);
                }
            }
            if (response.status() >= 400 && response.status() <= 499) {
                return new CompasClientException(response.status(), errorVM);
            }
            if (response.status() >= 500 && response.status() <= 599) {
                return new CompasUploadException(response.status(), errorVM);
            }
            return decoder.decode(methodKey, response);
        };
    }
}
