package no.unit.nva.biobank.model.nva;

import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.model.ApprovalStatus;
import nva.commons.core.JacocoGenerated;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Objects;

public class BiobankApproval implements JsonSerializable {

    private final Instant approvedDate;
    private final String approvedBy;
    private final ApprovalStatus approvedStatus;
    private final String applicationCode;
    private final String approvalReferenceId;

    @JacocoGenerated
    @ConstructorProperties({"approvedDate","approvedBy","approvedStatus","applicationCode","approvalReferenceId"})
    public BiobankApproval(Instant approvedDate, String approvedBy, ApprovalStatus approvedStatus,
                           String applicationCode, String approvalReferenceId) {
        this.approvedDate = approvedDate;
        this.approvedBy = approvedBy;
        this.approvedStatus = approvedStatus;
        this.applicationCode = applicationCode;
        this.approvalReferenceId = approvalReferenceId;
    }

    /**
     * Constructor based on cristin model.
     * @param cristinApproval - the corresponding field of Cristin model
     */
    public BiobankApproval(CristinApproval cristinApproval) {
        this.approvedDate = cristinApproval.getApprovedDate();
        this.approvedBy = cristinApproval.getApprovedBy();
        this.approvedStatus = ApprovalStatus.fromValue(cristinApproval.getApprovalStatus());
        this.applicationCode = cristinApproval.getApplicationCode();
        this.approvalReferenceId = cristinApproval.getApprovalReferenceId();
    }

    public Instant getApprovedDate() {
        return approvedDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public ApprovalStatus getApprovedStatus() {
        return approvedStatus;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public String getApprovalReferenceId() {
        return approvalReferenceId;
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BiobankApproval)) {
            return false;
        }
        BiobankApproval that = (BiobankApproval) o;
        return Objects.equals(getApprovedDate(), that.getApprovedDate())
            && Objects.equals(getApprovedBy(), that.getApprovedBy())
            && Objects.equals(getApprovedStatus(), that.getApprovedStatus())
            && Objects.equals(getApplicationCode(), that.getApplicationCode())
            && Objects.equals(getApprovalReferenceId(), that.getApprovalReferenceId());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getApprovedDate(), getApprovedBy(), getApprovedStatus(), getApplicationCode(),
            getApprovalReferenceId());
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }
}
