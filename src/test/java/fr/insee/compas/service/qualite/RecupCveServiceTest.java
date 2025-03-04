package fr.insee.compas.service.qualite;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.insee.compas.service.GitlabService;
import fr.insee.compas.service.OscarService;
import fr.insee.compas.service.TableFaitsService;

@ExtendWith(MockitoExtension.class)
class RecupCveServiceTest {

    @Mock private OscarService oscarService;

    @Mock private GitlabService gitlabService;

    @Mock private UtilsCveService utilsService;

    @Mock private TableFaitsService tableFaitsService;

    @InjectMocks private RecupCveService cveService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testGetCveFromJson() throws JsonProcessingException {

        Map<String, Set<String>> expectedMap = new HashMap<>();
        expectedMap.put("CRITICAL", new HashSet<>(Arrays.asList("value1", "value2")));

        // Création d'un JSON simulé
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.set("Results", JsonNodeFactory.instance.arrayNode().add("testCve"));

        String jsonString = objectMapper.writeValueAsString(rootNode);

        when(utilsService.parseResults(any())).thenReturn(expectedMap);
        // Appeler la méthode et récupérer les résultats
        // Appel de la méthode testée
        Map<String, Set<String>> result = cveService.getCveFromJson(jsonString);
        // Vérifier les résultats
        // Vérification des résultats
        assertNotNull(result);
        assertEquals(expectedMap, result);
        verify(utilsService, times(1)).parseResults(any());
    }
}
