package fr.insee.compas.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.tomakehurst.wiremock.WireMockServer;

import fr.insee.compas.CompasApplication;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = CompasApplication.class)
@ActiveProfiles("integration")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WireMockConfig.class})
public class OscarClientITest {

    @Autowired private WireMockServer mockOscarService;

    @Autowired private OscarClient oscarClientIT;

    @BeforeEach
    void setUp() throws Exception {
        OscarMocks.setUpMockOscarResponse(mockOscarService);
        System.out.println("mock url  :" + mockOscarService.baseUrl());
    }

    @Test
    public void testApplicationSirene4() {
        assertEquals("sirene4", oscarClientIT.getApplicationOscar(123).getBody().getNom());
    }
}
