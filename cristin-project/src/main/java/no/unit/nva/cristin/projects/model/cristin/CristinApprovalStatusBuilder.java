package no.unit.nva.cristin.projects.model.cristin;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.projects.model.nva.EnumBuilder.mapValuesReversed;
import java.util.Map;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.model.ApprovalStatus;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;

public class CristinApprovalStatusBuilder implements EnumBuilder<CristinApproval, ApprovalStatus> {

    private static final Map<String, String> mapper = mapValues();

    public static final String CRISTIN_NOT_APPLIED = "NOTAPPLIED";
    public static final String NOT_APPLIED = "NotApplied";
    public static final String CRISTIN_APPLIED = "APPLIED";
    public static final String APPLIED = "Applied";
    public static final String CRISTIN_APPROVED = "APPROVED";
    public static final String APPROVED = "Approved";
    public static final String CRISTIN_DECLINED = "DECLINED";
    public static final String DECLINED = "Declined";
    public static final String CRISTIN_REJECTION = "REJECTION";
    public static final String REJECTED = "Rejected";

    @Override
    public ApprovalStatus build(CristinApproval cristinApproval) {
        if (isNull(cristinApproval) || isNull(cristinApproval.getApprovalStatus())) {
            return null;
        }
        var value = cristinApproval.getApprovalStatus();
        return ApprovalStatus.fromValue(mapper.get(value));
    }

    /**
     * Lookup Cristin value from model.
     */
    public static String reverseLookup(ApprovalStatus approvalStatus) {
        if (isNull(approvalStatus)) {
            return null;
        }
        return mapValuesReversed(mapper).get(approvalStatus.getStatusValue());
    }

    private static Map<String, String> mapValues() {
        return Map.of(CRISTIN_NOT_APPLIED, NOT_APPLIED,
                      CRISTIN_APPLIED, APPLIED,
                      CRISTIN_APPROVED, APPROVED,
                      CRISTIN_DECLINED, DECLINED,
                      CRISTIN_REJECTION, REJECTED);
    }
}
