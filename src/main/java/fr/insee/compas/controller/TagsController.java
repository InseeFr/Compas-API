package fr.insee.compas.controller;

import static fr.insee.compas.util.UtilsDemande.parseDate;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.compas.dto.gitlab.TagsGitLabDto;
import fr.insee.compas.service.gitservice.IGitlabService;
import fr.insee.compas.view.TagsView;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/tags")
public class TagsController {

    private final IGitlabService gitlabService;

    public TagsController(IGitlabService gitlabService) {
        this.gitlabService = gitlabService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TagsView> getTags() {
        log.info("Récupération des tags de l'api et de l'ihm");
        TagsGitLabDto tagsGitLabDto = gitlabService.getLatestTags();

        TagsView.ApiTagView apiTagView =
                Optional.ofNullable(tagsGitLabDto.getApiTag())
                        .map(
                                tag ->
                                        new TagsView.ApiTagView(
                                                tag.name(), parseDate(tag.created_at())))
                        .orElse(null);

        TagsView.IhmTagView ihmTagView =
                Optional.ofNullable(tagsGitLabDto.getIhmTag())
                        .map(
                                tag ->
                                        new TagsView.IhmTagView(
                                                tag.name(), parseDate(tag.created_at())))
                        .orElse(null);

        return ResponseEntity.ok(
                TagsView.builder().ihmTagView(ihmTagView).apiTagView(apiTagView).build());
    }
}
