package no.unit.nva.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.unit.nva.model.cristin.CristinBiobankApprovals;

import java.time.Instant;

public class BiobankApprovals {


    public static final String APPROVED_DATE = "approvedDate";
    public static final String APPROVED_BY = "approvedBy";

    public static final String APPROVAL_STATUS = "approvalStatus";
    public static final String APPLICATION_CODE = "applicationCode";
    public static final String APPROVAL_REFERENCE_ID = "approvalReferenceId";

    @JsonProperty(APPROVED_DATE)
    private final Instant approvedDate;
    @JsonProperty (APPROVED_BY)
    private final String approvedBy;
    @JsonProperty (APPROVAL_STATUS)
    private final String approvedSTatus;
    @JsonProperty (APPLICATION_CODE)
    private final String applicationCode;
    @JsonProperty (APPROVAL_REFERENCE_ID)
    private final String approvalReferenceId;


    public BiobankApprovals(Instant approvedDate,
                                   String approvedBy,
                                   String approvedSTatus,
                                   String applicationCode,
                                   String approvalReferenceId) {
        this.approvedDate = approvedDate;
        this.approvedBy = approvedBy;
        this.approvedSTatus = approvedSTatus;
        this.applicationCode = applicationCode;
        this.approvalReferenceId = approvalReferenceId;
    }

    public BiobankApprovals (CristinBiobankApprovals cristinApprovals) {
        this.approvedDate = cristinApprovals.getApprovedDate();
        this.approvedBy = cristinApprovals.getApprovedBy();
        this.approvedSTatus = cristinApprovals.getApprovedSTatus();
        this.applicationCode = cristinApprovals.getApplicationCode();
        this.approvalReferenceId = cristinApprovals.getApprovalReferenceId();
    }

    public Instant getApprovedDate() {
        return approvedDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getApprovedSTatus() {
        return approvedSTatus;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public String getApprovalReferenceId() {
        return approvalReferenceId;
    }
}
