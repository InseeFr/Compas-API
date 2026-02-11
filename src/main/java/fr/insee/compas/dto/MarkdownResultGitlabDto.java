package fr.insee.compas.dto;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarkdownResultGitlabDto {
    private BigInteger pageMetaId;
    private String format;
    private String slug;
    private String title;
    private String content;
    private String encoding;
}
