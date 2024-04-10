package no.unit.nva.cristin.organization.common.client.v20230526;

import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static nva.commons.core.attempt.Try.attempt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.core.attempt.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationSubUnitEnricher {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationSubUnitEnricher.class);

    public static final String ERROR_MSG_ENRICH_FAILED = "Enriching of query result failed. Please try again. Result "
                                                         + "that failed is : {}";
    public static final String EXCEPTION_THAT_WAS_THROWN = "Exception thrown: ";

    private final List<Organization> inputOrganizations;
    private final Map<String, String> queryParams;
    private final FetchApiClient<Map<String, String>, Organization> fetchClient;
    private final List<Organization> enrichedOrganizations;

    public OrganizationSubUnitEnricher(List<Organization> inputOrganizations,
                                       Map<String, String> queryParams,
                                       FetchApiClient<Map<String, String>, Organization> fetchClient) {
        this.inputOrganizations = inputOrganizations;
        this.queryParams = queryParams;
        this.fetchClient = fetchClient;
        enrichedOrganizations = new ArrayList<>();
    }

    public OrganizationSubUnitEnricher enrich() {
        inputOrganizations.forEach(organization -> {
            var identifier = extractIdentifier(organization);
            var enrichParams = extractParams(identifier);
            var enriched = executeFetchUsingParams(enrichParams);

            enrichedOrganizations.add(enriched);
        });

        return this;
    }

    public List<Organization> getResult() {
        return enrichedOrganizations;
    }

    private static String extractIdentifier(Organization organization) {
        return attempt(() -> UriUtils.extractLastPathElement(organization.getId())).orElseThrow();
    }

    private Map<String, String> extractParams(String identifier) {
        var enrichParams = new ConcurrentHashMap<String, String>();
        enrichParams.put(DEPTH, queryParams.get(DEPTH));
        enrichParams.put(IDENTIFIER, identifier);

        return enrichParams;
    }

    private Organization executeFetchUsingParams(Map<String, String> enrichParams) {
        // TODO: Keep original exception instead of using RuntimeException
        return attempt(() -> fetchClient.executeFetch(enrichParams)).orElseThrow(this::logAndThrow);
    }

    private RuntimeException logAndThrow(Failure<Organization> fail) {
        logger.error(ERROR_MSG_ENRICH_FAILED, extractFailedOrgId(fail));
        logger.error(EXCEPTION_THAT_WAS_THROWN, fail.getException());

        return new RuntimeException(ERROR_MSG_ENRICH_FAILED);
    }

    private static String extractFailedOrgId(Failure<Organization> fail) {
        return Optional.ofNullable(fail)
                   .map(Failure::get)
                   .map(Organization::getId)
                   .map(Objects::toString)
                   .orElse(null);
    }

}
