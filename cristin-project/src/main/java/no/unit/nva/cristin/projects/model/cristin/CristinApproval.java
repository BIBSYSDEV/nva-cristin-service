package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.Instant;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.utils.CustomInstantSerializer;

public class CristinApproval implements JsonSerializable {

    public static final String APPROVED_DATE = "approved_date";
    public static final String APPROVED_BY = "approved_by";
    public static final String APPROVAL_STATUS = "approval_status";
    public static final String APPLICATION_CODE = "application_code";
    public static final String APPROVAL_REFERENCE_ID = "approval_reference_id";

    @JsonProperty(APPROVED_DATE)
    @JsonSerialize(using = CustomInstantSerializer.class)
    private final Instant approvedDate;
    @JsonProperty(APPROVED_BY)
    private final String approvedBy;
    @JsonProperty(APPROVAL_STATUS)
    private final String approvalStatus;
    @JsonProperty(APPLICATION_CODE)
    private final String applicationCode;
    @JsonProperty(APPROVAL_REFERENCE_ID)
    private final String approvalReferenceId;

    @JsonCreator
    public CristinApproval(@JsonProperty(APPROVED_DATE) Instant approvedDate,
                           @JsonProperty(APPROVED_BY) String approvedBy,
                           @JsonProperty(APPROVAL_STATUS) String approvalStatus,
                           @JsonProperty(APPLICATION_CODE) String applicationCode,
                           @JsonProperty(APPROVAL_REFERENCE_ID) String approvalReferenceId) {
        this.approvedDate = approvedDate;
        this.approvedBy = approvedBy;
        this.approvalStatus = approvalStatus;
        this.applicationCode = applicationCode;
        this.approvalReferenceId = approvalReferenceId;
    }

    public Instant getApprovedDate() {
        return approvedDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public String getApprovalReferenceId() {
        return approvalReferenceId;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinApproval)) {
            return false;
        }
        CristinApproval that = (CristinApproval) o;
        return Objects.equals(getApprovedDate(), that.getApprovedDate())
               && Objects.equals(getApprovedBy(), that.getApprovedBy())
               && Objects.equals(getApprovalStatus(), that.getApprovalStatus())
               && Objects.equals(getApplicationCode(), that.getApplicationCode())
               && Objects.equals(getApprovalReferenceId(), that.getApprovalReferenceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApprovedDate(), getApprovedBy(), getApprovalStatus(), getApplicationCode(),
                            getApprovalReferenceId());
    }
}
