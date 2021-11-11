package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.common.model.SearchResponse;
import no.unit.nva.cristin.model.nva.Organization;
import no.unit.nva.cristin.organization.exception.HttpClientFailureException;
import no.unit.nva.cristin.organization.exception.NonExistingUnitError;
import no.unit.nva.cristin.organization.utils.Language;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static no.unit.nva.cristin.projects.Constants.BASE_PATH;
import static no.unit.nva.cristin.projects.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.projects.Constants.HTTPS;
import static no.unit.nva.cristin.projects.Constants.ORGANIZATION_PATH;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    /**
     * Get information for a Organization.
     *
     * @param uri      the Cristin unit URI
     * @param language a language code for the details of each unit
     * @return an {@link Organization} containing the information
     * @throws NonExistingUnitError       when the URI does not correspond to an existing unit.
     * @throws HttpClientFailureException when Cristin server reports failure
     */
    public Organization getSingleUnit(URI uri, Language language)
            throws NonExistingUnitError, HttpClientFailureException {
        logger.info("Fetching results for: " + uri.toString());
        throw new NonExistingUnitError(uri.toString());
    }

    /**
     * Fetch Organizations matching given query criteria.
     * @param requestQueryParams Map containing verified query parameters
     */
    public SearchResponse<Organization> queryInstitutions(Map<String, String> requestQueryParams) {
        return new SearchResponse(createOrganizationUri("3232")) // TODO Create Id from requestQueryParams
                .withProcessingTime(0L)
                .withSize(0)
                .withHits(Collections.emptyList());
    }

    private URI createOrganizationUri(String identifier) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(ORGANIZATION_PATH)
                .addChild(identifier).getUri();
    }
}
