package no.unit.nva.clinicaltrial.rest;

import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.utils.UriUtils.getNvaApiId;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;
import no.unit.nva.clinicaltrial.model.ClinicalTrial;
import no.unit.nva.clinicaltrial.model.Reportable;
import nva.commons.core.paths.UriWrapper;

@JsonTypeName("ClinicalTrial")
public record ClinicalTrialResponse(URI id, URI project, Reportable reportable) {

  private static final String CLINICAL_TRIAL_PATH = "clinical-trial";

  public static ClinicalTrialResponse from(ClinicalTrial clinicalTrial) {
    var projectId = getNvaApiId(clinicalTrial.projectIdentifier(), PROJECT_PATH_NVA);
    return new ClinicalTrialResponse(
        UriWrapper.fromUri(projectId).addChild(CLINICAL_TRIAL_PATH).getUri(),
        projectId,
        clinicalTrial.reportable());
  }
}
