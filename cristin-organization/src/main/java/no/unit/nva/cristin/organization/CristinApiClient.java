package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.exception.HttpClientFailureException;
import no.unit.nva.exception.NonExistingUnitError;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static no.unit.nva.utils.UriUtils.createUriFromParams;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);
    private transient final HttpExecutor httpExecutor = new HttpExecutorImpl();

    /**
     * Get information for an Organization.
     *
     * @param uri      the Cristin unit URI
     * @return an {@link Organization} containing the information
     * @throws NonExistingUnitError       when the URI does not correspond to an existing unit.
     * @throws HttpClientFailureException when Cristin server reports failure
     */
    public Organization getSingleUnit(URI uri)
            throws NonExistingUnitError, HttpClientFailureException, InterruptedException {
        logger.info("Fetching results for: " + uri.toString());
        return httpExecutor.getSingleUnit(uri, null);
    }

    /**
     * Fetch Organizations matching given query criteria.
     * @param requestQueryParams Map containing verified query parameters
     */
    public SearchResponse<Organization> queryInstitutions(Map<String, String> requestQueryParams) {
        return new SearchResponse<Organization>(createUriFromParams(requestQueryParams, UriUtils.INSTITUTION))
                .withProcessingTime(0L)
                .withSize(0)
                .withHits(Collections.emptyList());
    }
}
