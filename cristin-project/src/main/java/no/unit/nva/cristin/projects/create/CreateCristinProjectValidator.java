package no.unit.nva.cristin.projects.create;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import no.unit.nva.Validator;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

public class CreateCristinProjectValidator implements Validator<NvaProject> {

    protected enum ValidatedResult {
        Empty("project data required"),
        HasId("project identifier not allowed"),
        NoTitle("title required"),
        InvalidStartDate("start date invalid"),
        HasNoContributors("contributors required"),
        HasNoCoordinatingOrganization("coordinating organization required");
        public final String label;

        ValidatedResult(String label) {
            this.label = label;
        }
    }

    @Override
    public void validate(NvaProject nvaProject) throws ApiGatewayException {
        validateRequiredInput(nvaProject);
    }

    private void validateRequiredInput(NvaProject project) throws BadRequestException {
        var validatedResult = validateProjectInput(project);
        if (!validatedResult.isEmpty()) {
            var validateDescriptions =
                validatedResult.stream()
                    .map(result -> result.label)
                    .collect(Collectors.joining(", "," (", ")"));
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD + validateDescriptions);
        }
    }

    private Set<ValidatedResult> validateProjectInput(NvaProject project) {
        var results = new HashSet<ValidatedResult>();
        if (isNull(project)) {
            results.add(ValidatedResult.Empty);
            return results;
        }
        if (hasId(project)) {
            results.add(ValidatedResult.HasId);
        }
        if (noTitle(project)) {
            results.add(ValidatedResult.NoTitle);
        }
        if (invalidStartDate(project.getStartDate())) {
            results.add(ValidatedResult.InvalidStartDate);
        }
        if (hasNoContributors(project.getContributors())) {
            results.add(ValidatedResult.HasNoContributors);
        }
        if (hasNoCoordinatingOrganization(project.getCoordinatingInstitution())) {
            results.add(ValidatedResult.HasNoCoordinatingOrganization);
        }
        return results;
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
}
