package no.unit.nva.cristin.projects.create;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import no.unit.nva.Validator;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

public class CreateCristinProjectValidator implements Validator<NvaProject> {

    public static final Set<String> validClinicalTrialPhases = Set.of("1", "2", "3", "4");
    public static final Set<String> validHealthProjectTypes = Set.of("DRUGSTUDY", "OTHERCLIN", "OTHERSTUDY");
    public static final String INVALID_CLINICAL_TRIAL_PHASE =
        "Clinical Trial Phase is invalid, can only contain the following values: ";
    public static final String INVALID_HEALTH_PROJECT_TYPE =
        "Health Project Type is invalid, can only contain the following values: ";

    protected enum ValidatedResult {
        Ok("no errors"),
        Empty(" (project data required)"),
        HasId(" (project identifier not allowed)"),
        NoTitle(" (title required)"),
        InvalidStartDate(" (start date invalid)"),
        HasNoContributors(" (contributors required)"),
        HasNoCoordinatingOrganization(" (coordinating organization required)");
        public final String label;

        ValidatedResult(String label) {
            this.label = label;
        }
    }

    @Override
    public void validate(NvaProject nvaProject) throws ApiGatewayException {
        validateRequiredInput(nvaProject);
        validateOptionalInput(nvaProject);
    }

    private void validateRequiredInput(NvaProject project) throws BadRequestException {
        var validatedResult = validateProjectInput(project);
        if (!validatedResult.equals(ValidatedResult.Ok)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD + validatedResult.label);
        }
    }

    private ValidatedResult validateProjectInput(NvaProject project) {
        if (isNull(project)) {
            return ValidatedResult.Empty;
        }
        if (hasId(project)) {
            return ValidatedResult.HasId;
        }
        if (noTitle(project)) {
            return ValidatedResult.NoTitle;
        }
        if (invalidStartDate(project.getStartDate())) {
            return ValidatedResult.InvalidStartDate;
        }
        if (hasNoContributors(project.getContributors())) {
            return ValidatedResult.HasNoContributors;
        }
        if (hasNoCoordinatingOrganization(project.getCoordinatingInstitution())) {
            return ValidatedResult.HasNoCoordinatingOrganization;
        }
        return ValidatedResult.Ok;
    }

    private boolean hasNoCoordinatingOrganization(Organization coordinatingInstitution) {
        return isNull(coordinatingInstitution);
    }

    private boolean hasNoContributors(List<NvaContributor> contributors) {
        return isNull(contributors) || contributors.isEmpty();
    }

    private boolean invalidStartDate(Instant startDate) {
        return isNull(startDate);
    }

    private boolean hasId(NvaProject project) {
        return nonNull(project.getId());
    }

    private boolean noTitle(NvaProject project) {
        return StringUtils.isEmpty(project.getTitle());
    }

    private void validateOptionalInput(NvaProject nvaProject) throws BadRequestException {
        if (nonNull(nvaProject.getHealthProjectData())) {
            validateHealthData(nvaProject.getHealthProjectData());
        }
    }

    private void validateHealthData(HealthProjectData healthData) throws BadRequestException {
        if (nonNull(healthData.getType()) && !validHealthProjectTypes.contains(healthData.getType())) {
            throw exceptionInvalidHealthProjectType();
        }
        if (nonNull(healthData.getClinicalTrialPhase())
            && !validClinicalTrialPhases.contains(healthData.getClinicalTrialPhase())) {
            throw exceptionInvalidClinicalTrialPhase();
        }
    }

    private BadRequestException exceptionInvalidHealthProjectType() {
        return new BadRequestException(INVALID_HEALTH_PROJECT_TYPE
                                       + String.join(" ; ", validHealthProjectTypes));
    }

    private BadRequestException exceptionInvalidClinicalTrialPhase() {
        return new BadRequestException(INVALID_CLINICAL_TRIAL_PHASE
                                       + String.join(" ; ", validClinicalTrialPhases));
    }
}
