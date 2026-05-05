package fr.insee.compas.service.scheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.insee.compas.model.mail.Mail;
import fr.insee.compas.service.spoc.SpocService;
import fr.insee.compas.util.mail.templates.TemplateMailErreurScheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailErreurSchedulerService implements IMailErreurScheduler {

    @Value("${receiver.erreur.scheduler}")
    private List<String> receiverErreurScheduler;

    @Value("${receiver.erreur.scheduler.adj}")
    private List<String> receiverErreurSchedulerAdj;

    private final SpocService spocService;
    private final TemplateMailErreurScheduler templateMailErreurScheduler;

    public void sendMailErreurScheduler(File file, boolean test) {
        if (!file.exists() || file.length() == 0) {
            log.warn(
                    "Fichier pour l'envoi du mail en cas d'erreur sur le scheduler est inexistant");
            deleteFile(file);
            return;
        }

        log.info("Fichier existant {} avec une taille de {} octets", file.getName(), file.length());
        Mail mail = new Mail();
        mail.setObject(templateMailErreurScheduler.getSubjectTemplateMail());
        mail.setMessage(
                templateMailErreurScheduler.getBodyTemplateMail(
                        receiverErreurScheduler, receiverErreurSchedulerAdj, test));
        mail.setTo(test ? List.of() : receiverErreurScheduler);
        mail.setCc(test ? List.of() : receiverErreurSchedulerAdj);
        mail.setAttachments(List.of(file));
        spocService.sendMail(mail);
        deleteFile(file);
    }

    private void deleteFile(File file) {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            log.error("Impossible de supprimer le fichier d'erreurs : {}", file.getName());
        }
    }
}
