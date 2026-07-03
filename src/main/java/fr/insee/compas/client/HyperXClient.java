package fr.insee.compas.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import fr.insee.compas.client.configuration.CustomErrorDecoder;
import fr.insee.compas.client.configuration.HyperXFeignConfig;
import fr.insee.compas.client.view.ApplishareHyperXView;

@FeignClient(
        name = "hyperClient",
        url = "${fr.insee.compas.url.hyperx}",
        configuration = {CustomErrorDecoder.class, HyperXFeignConfig.class})
public interface HyperXClient {
    @GetMapping(
            "applishare?list_colonnes=application,plateforme,taille_applishare_go,taille_applishare_tot_go")
    ResponseEntity<List<ApplishareHyperXView>> getApplishareHyperX();
}
