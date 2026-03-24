package fr.insee.compas.dto.gitlab;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TagsGitLabDto {

    TagApi apiTag;
    TagIhm ihmTag;

    public record TagApi(String name, String created_at) {}

    public record TagIhm(String name, String created_at) {}
}
