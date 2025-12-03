package fr.insee.compas.model.compas;

import lombok.Getter;

@Getter
public enum IndicateurType {
    NBR_LIGNE(1),
    NBR_LIGNE_TEST(2),
    CVE_CRITICAL(3),
    CVE_HIGH(4),
    CVE_MEDIUM(5),
    CVE_LOW(6),
    CVE_CRITICAL_APPLI(7),
    CVE_HIGH_APPLI(8),
    CVE_MEDIUM_APPLI(9),
    CVE_LOW_APPLI(10),
    RAM_ALLOUEE(201),
    RAM_MAXI(202),
    DISQUE_ALLOUE(203),
    DISQUE_CONSOMME(204),
    CPU_ALLOUEE(205),
    CPU_MAXI(206),
    CONSO_ELEC(207),
    NBR_VM(208),
    RAM_ALLOUEE_PD(211),
    RAM_MAXI_PD(212),
    DISQUE_ALLOUE_PD(213),
    DISQUE_CONSOMME_PD(214),
    CPU_ALLOUEE_PD(215),
    CPU_MAXI_PD(216),
    CONSO_ELEC_PD(217),
    NBR_VM_PD(218),
    NBR_JOUR_MEP(301),
    DEPLOYMENT_COUNT(302),
    NBR_CONTRIBUTIONS_PROJET(303),
    DETTE_TECH(11),
    FIABILITE(12),
    DELAI_MAJ_VM(101),
    NB_VM_NON_MISES_A_JOUR(102),
    ISSUE_ACCESSIBILITY(501);

    private final int value;

    IndicateurType(int value) {
        this.value = value;
    }
}
