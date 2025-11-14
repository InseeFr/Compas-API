package fr.insee.compas.model.mail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mail {

    private String object;
    private String message;

    /** Destinataires principaux (To) */
    private List<String> to = new ArrayList<>();

    /** Copie carbone (Cc) */
    private List<String> cc = new ArrayList<>();

    public Mail() {}

    public Mail(String object, String message, Collection<String> to, Collection<String> cc) {
        this.object = object;
        this.message = message;
        this.to = new ArrayList<>(to);
        this.cc = new ArrayList<>(cc);
    }

    public void addTo(Collection<String> emails) {
        this.to.addAll(emails);
    }

    public void addCc(Collection<String> emails) {
        this.cc.addAll(emails);
    }
}
