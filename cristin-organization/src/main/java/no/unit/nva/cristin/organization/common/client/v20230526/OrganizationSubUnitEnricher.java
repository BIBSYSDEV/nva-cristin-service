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
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizationSubUnitEnricher {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationSubUnitEnricher.class);

    public static final String COULD_NOT_BE_FOUND_IN_UPSTREAM =
        "Organization from search result could not be found in upstream: {}";

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

    public OrganizationSubUnitEnricher enrich() throws ApiGatewayException {
        for (Organization organization : inputOrganizations) {
            var identifier = extractIdentifier(organization);
            var enrichParams = extractParams(identifier);
            try {
                var enriched = fetchClient.executeFetch(enrichParams);
                enrichedOrganizations.add(enriched);
            } catch (NotFoundException notFoundException) {
                logger.warn(COULD_NOT_BE_FOUND_IN_UPSTREAM, extractOrgId(organization));
            }
        }

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

    private static String extractOrgId(Organization organization) {
        return Optional.ofNullable(organization)
                   .map(Organization::getId)
                   .map(Objects::toString)
                   .orElse(null);
    }

}
