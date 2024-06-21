package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.projects.model.cristin.CristinApplicationCodeBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinApprovalAuthorityBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinApprovalStatusBuilder;
import no.unit.nva.cristin.projects.model.nva.ApplicationCode;
import no.unit.nva.cristin.projects.model.nva.Approval;
import no.unit.nva.cristin.projects.model.nva.ApprovalAuthority;
import no.unit.nva.cristin.projects.model.nva.EnumBuilder;
import no.unit.nva.model.ApprovalStatus;

public class CristinApprovalToApproval implements Function<CristinApproval, Approval> {

    private final transient EnumBuilder<CristinApproval, ApprovalAuthority> approvalAuthorityBuilder;
    private final transient EnumBuilder<CristinApproval, ApplicationCode> applicationCodeBuilder;
    private final transient EnumBuilder<CristinApproval, ApprovalStatus> approvalStatusBuilder;

    public CristinApprovalToApproval() {
        this.approvalAuthorityBuilder = new CristinApprovalAuthorityBuilder();
        this.applicationCodeBuilder = new CristinApplicationCodeBuilder();
        this.approvalStatusBuilder = new CristinApprovalStatusBuilder();
    }

    @Override
    public Approval apply(CristinApproval cristinApproval) {
        var authority = approvalAuthorityBuilder.build(cristinApproval);
        var applicationCode = applicationCodeBuilder.build(cristinApproval);
        var status = approvalStatusBuilder.build(cristinApproval);

        return new Approval(cristinApproval.getApprovedDate(), authority, status, applicationCode,
                            cristinApproval.getApprovalReferenceId(), cristinApproval.getApprovedByName());
    }

}
