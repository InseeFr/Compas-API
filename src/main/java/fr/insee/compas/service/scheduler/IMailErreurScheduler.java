package fr.insee.compas.service.scheduler;

import java.io.File;

public interface IMailErreurScheduler {
    void sendMailErreurScheduler(File file, boolean test);
}
