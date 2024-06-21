package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.model.CristinApproval;
import no.unit.nva.cristin.projects.model.cristin.CristinApplicationCodeBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinApprovalAuthorityBuilder;
import no.unit.nva.cristin.projects.model.cristin.CristinApprovalStatusBuilder;
import no.unit.nva.cristin.projects.model.nva.Approval;

public class ApprovalToCristinApproval implements Function<Approval, CristinApproval> {

    @Override
    public CristinApproval apply(Approval approval) {
        return new CristinApproval(approval.getDate(),
                                   CristinApprovalAuthorityBuilder.reverseLookup(approval.getAuthority()),
                                   CristinApprovalStatusBuilder.reverseLookup(approval.getStatus()),
                                   CristinApplicationCodeBuilder.reverseLookup(approval.getApplicationCode()),
                                   approval.getIdentifier(),
                                   approval.getAuthorityName());
    }

}
