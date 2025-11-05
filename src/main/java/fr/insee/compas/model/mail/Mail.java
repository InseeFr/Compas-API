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
    private List<String> receiver = new ArrayList<>();

    public Mail() {}

    public Mail(String object, String message, Collection<String> receivers) {
        this.object = object;
        this.message = message;
        this.receiver = new ArrayList<>(receivers);
    }

    public void addReceiver(Collection<String> emails) {
        this.receiver.addAll(emails);
    }
}
