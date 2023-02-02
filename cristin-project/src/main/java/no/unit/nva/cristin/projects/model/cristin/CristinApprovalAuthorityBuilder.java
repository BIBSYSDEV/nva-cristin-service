package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.projects.model.nva.EnumBuilder.mapValuesReversed;
import java.util.Map;
import no.unit.nva.cristin.projects.model.nva.ApprovalAuthority;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;

public class CristinApprovalAuthorityBuilder implements EnumBuilder<CristinApproval, ApprovalAuthority> {

    private static final Map<String, String> mapper = mapValues();

    public static final String CRISTIN_REK = "REK";
    public static final String REK = "Rek";
    public static final String CRISTIN_NARA = "NARA";
    public static final String NARA = "Nara";
    public static final String CRISTIN_NDPA = "NDPA";
    public static final String NDPA = "Ndpa";
    public static final String CRISTIN_NMA = "NMA";
    public static final String NMA = "Nma";
    public static final String CRISTIN_DIRHEALTH = "DIRHEALTH";
    public static final String DIRHEALTH = "Dirhealth";

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
        return Map.of(CRISTIN_REK, REK,
                      CRISTIN_NARA, NARA,
                      CRISTIN_NDPA, NDPA,
                      CRISTIN_NMA, NMA,
                      CRISTIN_DIRHEALTH, DIRHEALTH);
    }
}
