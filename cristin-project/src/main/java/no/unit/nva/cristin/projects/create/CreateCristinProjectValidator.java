package no.unit.nva.cristin.projects.create;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import no.unit.nva.Validator;
import no.unit.nva.cristin.projects.model.nva.ClinicalTrialPhase;
import no.unit.nva.cristin.projects.model.nva.HealthProjectData;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

public class CreateCristinProjectValidator implements Validator<NvaProject> {

    public static final Set<String> validHealthProjectTypes = Set.of("DRUGSTUDY", "OTHERCLIN", "OTHERSTUDY");
    public static final String INVALID_HEALTH_PROJECT_TYPE =
        "Health Project Type is invalid, can only contain the following values: ";

    @Override
    public void validate(NvaProject nvaProject) throws ApiGatewayException {
        validateRequiredInput(nvaProject);
        validateOptionalInput(nvaProject);
    }

    private void validateRequiredInput(NvaProject project) throws BadRequestException {
        if (isNull(project)
            || hasId(project)
            || noTitle(project)
            || invalidStartDate(project.getStartDate())
            || hasNoContributors(project.getContributors())
            || hasNoCoordinatingOrganization(project.getCoordinatingInstitution())
        ) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
        }
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
        if (ClinicalTrialPhase.hasValueInvalid(healthData.getClinicalTrialPhase())) {
            throw ClinicalTrialPhase.valueNotFoundException();
        }
    }

    private BadRequestException exceptionInvalidHealthProjectType() {
        return new BadRequestException(INVALID_HEALTH_PROJECT_TYPE
                                       + String.join(" ; ", validHealthProjectTypes));
    }

}