package no.unit.nva.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class CristinBiobankApprovals {


    public static final String CRISTIN_APPROVED_DATE = "approved_date";
    public static final String CRISTIN_APPROVED_BY = "approved_by";

    public static final String CRISTIN_APPROVAL_STATUS = "approval_status";
    public static final String CRISTIN_APPLICATION_CODE = "application_code";
    public static final String CRISTIN_APPROVAL_REFERENCE_ID = "approval_reference_id";

    @JsonProperty(CRISTIN_APPROVED_DATE)
    private final Instant approvedDate;
    @JsonProperty (CRISTIN_APPROVED_BY)
    private final String approvedBy;
    @JsonProperty (CRISTIN_APPROVAL_STATUS)
    private final String approvedSTatus;
    @JsonProperty (CRISTIN_APPLICATION_CODE)
    private final String applicationCode;

    @JsonProperty (CRISTIN_APPROVAL_REFERENCE_ID)
    private final String approvalReferenceId;


    public CristinBiobankApprovals(Instant approvedDate,
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
