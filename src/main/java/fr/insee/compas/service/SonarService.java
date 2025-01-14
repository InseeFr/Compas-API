package fr.insee.compas.service;

import java.io.IOException;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.insee.compas.model.sonar.*;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class SonarService {

    public static RecuperationMeasures getDataFromSonarAPIMeasures(
            String projetSonar, String metrics) throws IOException {
        final String urlParam = "measures/component";
        final String component = "?component=" + projetSonar;
        final String paramMetrics = "&metricKeys=" + metrics;
        String url = "http://sonar.insee.fr/api/" + urlParam + component + paramMetrics;
        // appel à l'API
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call monAppel = client.newCall(request);
        Response response = monAppel.execute();
        String jsonString = Objects.requireNonNull(response.body()).string();
        response.close();

        // construction de la classe à partir du fichier json renvoyer par l'API.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Cela indique à Jackson
        // d’ignorer silencieusement
        // les champs qu’il ne
        // reconnaît pas au lieu de
        // lever une exception.
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        return objectMapper.readValue(jsonString, RecuperationMeasures.class);
    }

    public static CouvertureTest getIndicateurSonar(String projet, String metric)
            throws IOException {
        CouvertureTest ce = new CouvertureTest();
        if (projet != null && !"Sans objet".equals(projet)) {
            RecuperationMeasures couverture =
                    SonarService.getDataFromSonarAPIMeasures(projet, metric);
            if (couverture.getComponent() != null
                    && !couverture.getComponent().getMeasures().isEmpty()) {
                couverture
                        .getComponent()
                        .getMeasures()
                        .forEach(
                                measure -> {
                                    ce.setProjet(projet);
                                    ce.setCouverture(measure.getValue());
                                });
            } else {
                ce.setProjet(projet);
                ce.setCouverture("Aucune couverture disponible");
            }
        }
        return ce;
    }
}
