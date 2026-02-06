package fr.insee.compas.service.meteo.alerte;

public interface ISendAlerteMeteo {
    void sendAlerteMeteo(int ageMinJours, boolean test);
}
