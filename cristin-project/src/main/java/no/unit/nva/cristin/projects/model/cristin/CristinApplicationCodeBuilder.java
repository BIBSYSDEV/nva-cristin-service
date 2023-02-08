package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.projects.model.nva.EnumBuilder.mapValuesReversed;
import java.util.Map;
import no.unit.nva.cristin.projects.model.nva.ApplicationCode;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;

public class CristinApplicationCodeBuilder implements EnumBuilder<CristinApproval, ApplicationCode> {

    private static final Map<String, String> mapper = mapValues();

    public static final String CRISTIN_ETICHAPPR = "ETICHAPPR";
    public static final String ETHICAL_APPROVAL = "EthicalApproval";
    public static final String CRISTIN_BIOBANK = "BIOBANK";
    public static final String BIO_BANK = "BioBank";
    public static final String CRISTIN_DRUGTRIAL = "DRUGTRIAL";
    public static final String DRUG_TRIAL = "DrugTrial";
    public static final String CRISTIN_BIOTECHN = "BIOTECHN";
    public static final String BIO_TECHNOLOGY = "BioTechnology";
    public static final String CRISTIN_MEDEQUIP = "MEDEQUIP";
    public static final String MEDICAL_EQUIPMENT = "MedicalEquipment";
    public static final String CRISTIN_TESTANIMAL = "TESTANIMAL";
    public static final String TEST_ANIMAL = "TestAnimal";
    public static final String CRISTIN_SENSINFO = "SENSINFO";
    public static final String SENSITIVE_INFORMATION = "SensitiveInformation";

    @Override
    public ApplicationCode build(CristinApproval cristinApproval) {
        if (isNull(cristinApproval) || isNull(cristinApproval.getApplicationCode())) {
            return null;
        }
        var value = cristinApproval.getApplicationCode();
        return ApplicationCode.fromValue(mapper.get(value));
    }

    /**
     * Lookup Cristin value from model.
     */
    public static String reverseLookup(ApplicationCode applicationCode) {
        if (isNull(applicationCode)) {
            return null;
        }
        return mapValuesReversed(mapper).get(applicationCode.getCodeValue());
    }

    private static Map<String, String> mapValues() {
        return Map.of(CRISTIN_ETICHAPPR, ETHICAL_APPROVAL,
                      CRISTIN_BIOBANK, BIO_BANK,
                      CRISTIN_DRUGTRIAL, DRUG_TRIAL,
                      CRISTIN_BIOTECHN, BIO_TECHNOLOGY,
                      CRISTIN_MEDEQUIP, MEDICAL_EQUIPMENT,
                      CRISTIN_TESTANIMAL, TEST_ANIMAL,
                      CRISTIN_SENSINFO, SENSITIVE_INFORMATION);
    }

}
