package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.projects.model.nva.EnumBuilder.mapValuesReversed;
import java.util.Map;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;
import no.unit.nva.cristin.projects.model.nva.HealthProjectType;

public class CristinHealthProjectTypeBuilder implements EnumBuilder<CristinProject, HealthProjectType> {

    private static final Map<String, String> mapper = mapValues();
    public static final String CRISTIN_DRUGSTUDY = "DRUGSTUDY";
    public static final String CRISTIN_OTHERCLIN = "OTHERCLIN";
    public static final String CRISTIN_OTHERSTUDY = "OTHERSTUDY";
    public static final String DRUGSTUDY = "Drugstudy";
    public static final String OTHERCLIN = "Otherclin";
    public static final String OTHERSTUDY = "Otherstudy";


    @Override
    public HealthProjectType build(CristinProject cristinProject) {
        if (isNull(cristinProject) || isNull(cristinProject.getHealthProjectType())) {
            return null;
        }
        var value = cristinProject.getHealthProjectType();
        return HealthProjectType.fromValue(mapper.get(value));
    }

    /**
     * Lookup Cristin value from model.
     */
    public static String reverseLookup(HealthProjectType healthProjectType) {
        if (isNull(healthProjectType)) {
            return null;
        }
        return mapValuesReversed(mapper).get(healthProjectType.getType());
    }

    private static Map<String, String> mapValues() {
        return Map.of(CRISTIN_DRUGSTUDY, DRUGSTUDY,
                      CRISTIN_OTHERCLIN, OTHERCLIN,
                      CRISTIN_OTHERSTUDY, OTHERSTUDY);
    }

}
