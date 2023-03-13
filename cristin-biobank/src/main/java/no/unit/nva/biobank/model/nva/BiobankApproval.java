package no.unit.nva.biobank.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.projects.model.cristin.CristinApproval;
import nva.commons.core.JacocoGenerated;

public class BiobankApproval implements JsonSerializable {

    @JsonProperty
    private Instant approvedDate;
    @JsonProperty
    private String approvedBy;
    @JsonProperty
    private String approvedStatus;
    @JsonProperty
    private String applicationCode;
    @JsonProperty
    private String approvalReferenceId;

    @JacocoGenerated
    public BiobankApproval() {
    }

    /**
     * Constructor based on cristin model.
     * @param cristinApproval - the corresponding field of Cristin model
     */
    public BiobankApproval(CristinApproval cristinApproval) {
        this.approvedDate = cristinApproval.getApprovedDate();
        this.approvedBy = cristinApproval.getApprovedBy();
        this.approvedStatus = cristinApproval.getApprovalStatus();
        this.applicationCode = cristinApproval.getApplicationCode();
        this.approvalReferenceId = cristinApproval.getApprovalReferenceId();
    }

    public Instant getApprovedDate() {
        return approvedDate;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getApprovedStatus() {
        return approvedStatus;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public String getApprovalReferenceId() {
        return approvalReferenceId;
    }
}
