package fr.insee.compas.view;

import java.time.LocalDate;

import lombok.Builder;

@Builder
public class TagsView {
    public final TagsView.ApiTagView apiTagView;
    public final TagsView.IhmTagView ihmTagView;

    public record ApiTagView(String tag, LocalDate createdAt) {}

    public record IhmTagView(String tag, LocalDate createdAt) {}
}
