package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.projects.model.nva.EnumBuilder.mapValuesReversed;
import java.util.Map;
import no.unit.nva.cristin.projects.model.nva.ApplicationCode;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;

public class CristinApplicationCodeBuilder implements EnumBuilder<CristinApproval, ApplicationCode> {

    private static final Map<String, String> mapper = mapValues();

    public static final String CRISTIN_ETICHAPPR = "ETICHAPPR";
    public static final String ETICHAPPR = "EtichAppr";
    public static final String CRISTIN_BIOBANK = "BIOBANK";
    public static final String BIOBANK = "Biobank";
    public static final String CRISTIN_DRUGTRIAL = "DRUGTRIAL";
    public static final String DRUGTRIAL = "DrugTrial";
    public static final String CRISTIN_BIOTECHN = "BIOTECHN";
    public static final String BIOTECHN = "BioTechN";
    public static final String CRISTIN_MEDEQUIP = "MEDEQUIP";
    public static final String MEDEQUIP = "MedEquip";
    public static final String CRISTIN_TESTANIMAL = "TESTANIMAL";
    public static final String TESTANIMAL = "TestAnimal";
    public static final String CRISTIN_SENSINFO = "SENSINFO";
    public static final String SENSINFO = "SensInfo";

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
        return Map.of(CRISTIN_ETICHAPPR, ETICHAPPR,
                      CRISTIN_BIOBANK, BIOBANK,
                      CRISTIN_DRUGTRIAL, DRUGTRIAL,
                      CRISTIN_BIOTECHN, BIOTECHN,
                      CRISTIN_MEDEQUIP, MEDEQUIP,
                      CRISTIN_TESTANIMAL, TESTANIMAL,
                      CRISTIN_SENSINFO, SENSINFO);
    }

}
