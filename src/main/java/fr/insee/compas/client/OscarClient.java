package fr.insee.compas.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import fr.insee.compas.client.configuration.CustomErrorDecoder;
import fr.insee.compas.client.configuration.oauth.CustomFeignAuthentification;
import fr.insee.compas.client.view.ApplicationOscarView;
import fr.insee.compas.client.view.ModuleOscarView;
import fr.insee.compas.client.view.VmOscarView;

@FeignClient(
        name = "oscarClient",
        url = "${oscar.url}",
        configuration = {CustomErrorDecoder.class, CustomFeignAuthentification.class})
public interface OscarClient {

    @GetMapping("/modules/{idModule}")
    ResponseEntity<ModuleOscarView> getModuleOscar(@PathVariable("idModule") Integer idModule);

    @GetMapping("/applications/{id}")
    ResponseEntity<ApplicationOscarView> getApplicationOscar(
            @PathVariable("id") Integer idApplication);

    @GetMapping("/modules")
    ResponseEntity<List<ModuleOscarView>> getAllModuleOscar();

    @GetMapping("/modules-historiques")
    ResponseEntity<String> getModuleHistoriqueOscar();

    @GetMapping("/virtual-machines")
    ResponseEntity<List<VmOscarView>> getAllVmOscar();
}
