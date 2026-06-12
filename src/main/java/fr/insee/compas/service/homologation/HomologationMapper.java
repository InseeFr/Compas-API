package fr.insee.compas.service.homologation;

import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.opencsv.bean.CsvToBeanBuilder;

import fr.insee.compas.exception.HomologationApiException;
import fr.insee.compas.model.homologation.Homologation;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HomologationMapper {
    @Value("${url.grist}")
    private String urlGrist;

    @Value("${fr.insee.compas.homologation.token.grist:}")
    private String gristToken;

    private final RestTemplate restTemplate;

    public HomologationMapper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Homologation> getAllHomologationCsv() {

        log.info("Récupération de la liste d'homologation via Grist");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + gristToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> reponse =
                    restTemplate.exchange(urlGrist, HttpMethod.GET, request, String.class);
            if (reponse.getBody() == null) return Collections.emptyList();
            Reader in = new StringReader(reponse.getBody());
            List<Homologation> h =
                    new CsvToBeanBuilder<Homologation>(in)
                            .withType(Homologation.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build()
                            .parse();
            h.forEach(
                    homologation -> { // majuscule en minuscule
                        if (homologation.getApsOscar() != null) {
                            homologation.setApsOscar(
                                    homologation.getApsOscar().toLowerCase().trim());
                        }
                    });
            log.info("Fin de récupération des homologations de grist");
            return h;
        } catch (HttpClientErrorException e) {
            throw new HomologationApiException(
                    "Erreur lors de la récupération des homologations sur Grist", e);
        }
    }

    public List<Homologation> getAllHomologationSep() {

        List<Homologation> allHomologations = getAllHomologationCsv();
        List<Homologation> result = new ArrayList<>();

        for (Homologation h : allHomologations) {
            String applications = h.getApsOscar();
            if (applications != null && !applications.isBlank()) {
                String[] apps = applications.split(",");
                for (String app : apps) {
                    Homologation homologation = buildHomologation(h, app);
                    result.add(homologation);
                }
            }
        }
        return result;
    }

    public Homologation buildHomologation(Homologation h, String app) {
        Homologation result = new Homologation();

        result.setApsOscar(app.trim());
        result.setSensitivity(h.getSensitivity());
        result.setHomologationSI(h.getStatutHomologation());
        result.setNomSI(h.getNomSI());

        switch (h.getStatutHomologation()) {
            case "non" -> {
                result.setStatutHomologation("non");
                result.setHomologationRemarks(h.getHomologationRemarks());
            }
            case "complète" -> {
                result.setStatutHomologation("homologuée");
                result.setHomologationBeginDate(h.getHomologationBeginDate());
                result.setHomologationEndDate(h.getHomologationEndDate());
                result.setHomologationRemarks(h.getHomologationRemarks());
            }
            case "partielle" -> buildHomologationPartielle(h, app, result);
            default ->
                    throw new IllegalArgumentException(
                            "Valeur inattendue pour le statut d'homologation : "
                                    + h.getStatutHomologation());
        }
        return result;
    }

    // cas homologation partielle
    public void buildHomologationPartielle(Homologation h, String app, Homologation result) {
        String appsHomologuees = h.getHomologationSI();
        if (appsHomologuees == null || appsHomologuees.isBlank()) return;

        boolean isHomologuee =
                Arrays.stream(appsHomologuees.split(","))
                        .map(String::trim)
                        .anyMatch(a -> a.equals(app.trim()));

        result.setHomologationRemarks(h.getHomologationRemarks());

        if (isHomologuee) {
            result.setStatutHomologation("homologuée");
            result.setHomologationBeginDate(h.getHomologationBeginDate());
            result.setHomologationEndDate(h.getHomologationEndDate());
        } else {
            result.setStatutHomologation("non");
        }
    }
}
