package fr.insee.compas.dto.devops;

import java.util.Set;

import fr.insee.compas.util.IndicatorSpecialValue;

public record AuthorResultDto(Set<AuthorsDto> authors, IndicatorSpecialValue specialValue) {

    public static AuthorResultDto of(Set<AuthorsDto> authors) {
        return new AuthorResultDto(authors, null);
    }

    public static AuthorResultDto special(IndicatorSpecialValue value) {
        return new AuthorResultDto(null, value);
    }

    public boolean isSpecial() {
        return specialValue != null;
    }
}
