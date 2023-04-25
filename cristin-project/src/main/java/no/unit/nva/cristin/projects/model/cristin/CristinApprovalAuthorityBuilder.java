package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.projects.model.nva.EnumBuilder.mapValuesReversed;
import java.util.Map;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.projects.model.nva.ApprovalAuthority;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;

public class CristinApprovalAuthorityBuilder implements EnumBuilder<CristinApproval, ApprovalAuthority> {

    private static final Map<String, String> mapper = mapValues();

    public static final String CRISTIN_REK = "REK";
    public static final String REGIONAL_ETHICAL_COMMITTEES = "RegionalEthicalCommittees";
    public static final String CRISTIN_NARA = "NARA";
    public static final String NORWEGIAN_ANIMAL_RESEARCH_AUTHORITY = "NorwegianAnimalResearchAuthority";
    public static final String CRISTIN_NDPA = "NDPA";
    public static final String NORWEGIAN_DATA_PROTECTION_AUTHORITY = "NorwegianDataProtectionAuthority";
    public static final String CRISTIN_NMA = "NMA";
    public static final String NORWEGIAN_MEDICINES_AGENCY = "NorwegianMedicinesAgency";
    public static final String CRISTIN_DIRHEALTH = "DIRHEALTH";
    public static final String NORWEGIAN_DIRECTORATE_OF_HEALTH = "NorwegianDirectorateOfHealth";

    @Override
    public ApprovalAuthority build(CristinApproval cristinApproval) {
        if (isNull(cristinApproval) || isNull(cristinApproval.getApprovedBy())) {
            return null;
        }
        var value = cristinApproval.getApprovedBy();
        return ApprovalAuthority.fromValue(mapper.get(value));
    }

    /**
     * Lookup Cristin value from model.
     */
    public static String reverseLookup(ApprovalAuthority authority) {
        if (isNull(authority)) {
            return null;
        }
        return mapValuesReversed(mapper).get(authority.getAuthorityValue());
    }

    private static Map<String, String> mapValues() {
        return Map.of(CRISTIN_REK, REGIONAL_ETHICAL_COMMITTEES,
                      CRISTIN_NARA, NORWEGIAN_ANIMAL_RESEARCH_AUTHORITY,
                      CRISTIN_NDPA, NORWEGIAN_DATA_PROTECTION_AUTHORITY,
                      CRISTIN_NMA, NORWEGIAN_MEDICINES_AGENCY,
                      CRISTIN_DIRHEALTH, NORWEGIAN_DIRECTORATE_OF_HEALTH);
    }
}
