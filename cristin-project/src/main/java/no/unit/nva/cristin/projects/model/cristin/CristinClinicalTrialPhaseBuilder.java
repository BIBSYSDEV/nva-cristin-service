package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.nva.ClinicalTrialPhase;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;

public class CristinClinicalTrialPhaseBuilder implements EnumBuilder<CristinProject, ClinicalTrialPhase> {

    private static final Map<String, String> mapper = mapValues();
    public static final String CRISTIN_PHASE_ONE = "1";
    public static final String PHASE_ONE = "PhaseI";
    public static final String CRISTIN_PHASE_TWO = "2";
    public static final String PHASE_TWO = "PhaseII";
    public static final String CRISTIN_PHASE_THREE = "3";
    public static final String PHASE_THREE = "PhaseIII";
    public static final String CRISTIN_PHASE_FOUR = "4";
    public static final String PHASE_FOUR = "PhaseIV";

    @Override
    public ClinicalTrialPhase build(CristinProject cristinProject) {
        if (isNull(cristinProject) || isNull(cristinProject.getClinicalTrialPhase())) {
            return null;
        }
        var value = cristinProject.getClinicalTrialPhase();
        return ClinicalTrialPhase.fromValue(mapper.get(value));
    }

    /**
     * Lookup Cristin value from model.
     */
    public static String reverseLookup(ClinicalTrialPhase clinicalTrialPhase) {
        if (isNull(clinicalTrialPhase)) {
            return null;
        }
        return mapValuesReversed().get(clinicalTrialPhase.getPhase());
    }

    private static Map<String, String> mapValues() {
        return Map.of(CRISTIN_PHASE_ONE, PHASE_ONE,
                      CRISTIN_PHASE_TWO, PHASE_TWO,
                      CRISTIN_PHASE_THREE, PHASE_THREE,
                      CRISTIN_PHASE_FOUR, PHASE_FOUR);
    }

    private static Map<String, String> mapValuesReversed() {
        return mapper.entrySet().stream()
                   .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));
    }
}
