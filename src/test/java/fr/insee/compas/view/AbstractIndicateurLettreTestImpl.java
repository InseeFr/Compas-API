package fr.insee.compas.view;

import java.util.List;

import lombok.Getter;

class AbstractIndicateurLettreTestImpl extends AbstractIndicateurLettreGlobale {
    @Getter private String lettreGlobale;
    private final List<String> lettres;

    public AbstractIndicateurLettreTestImpl(List<String> lettres) {
        this.lettres = lettres;
    }

    @Override
    protected List<String> getLettresPourCalcul() {
        return lettres;
    }

    @Override
    protected void setLettreGlobale(String lettre) {
        this.lettreGlobale = lettre;
    }
}
