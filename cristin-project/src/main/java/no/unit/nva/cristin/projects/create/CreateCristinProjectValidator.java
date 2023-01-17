package no.unit.nva.cristin.projects.create;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static nva.commons.core.attempt.Try.attempt;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import no.unit.nva.cristin.projects.common.ProjectValidator;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

public class CreateCristinProjectValidator implements ProjectValidator {

    @Override
    public <T> void validate(T classOfT) throws ApiGatewayException {
        var nvaProject = attempt(() -> (NvaProject) classOfT).orElseThrow();
        validateInput(nvaProject);
    }

    private void validateInput(NvaProject project) throws BadRequestException {
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
        return Objects.nonNull(project.getId());
    }

    private boolean noTitle(NvaProject project) {
        return StringUtils.isEmpty(project.getTitle());
    }

}
