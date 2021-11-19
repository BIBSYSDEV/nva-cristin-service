package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.paths.UriWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;


public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);
    private final transient HttpExecutor httpExecutor = new HttpExecutorImpl();

    /**
     * Get information for an Organization.
     *
     * @param uri the Cristin unit URI
     * @return an {@link Organization} containing the information
     * @throws NotFoundException when the URI does not correspond to an existing unit.
     */
    public Organization getSingleUnit(URI uri)
            throws NotFoundException, InterruptedException, FailedHttpRequestException {
        logger.info("Fetching results for: " + uri.toString());
        return httpExecutor.getNestedInstitution(uri);
    }

    /**
     * Fetch Organizations matching given query criteria.
     *
     * @param requestQueryParams Map containing verified query parameters
     */
    public SearchResponse<Organization> queryInstitutions(Map<String, String> requestQueryParams) {
        return new SearchResponse<Organization>(
                new UriWrapper(HTTPS, DOMAIN_NAME)
                        .addChild(BASE_PATH)
                        .addChild(UriUtils.INSTITUTION)
                        .addQueryParameters(requestQueryParams)
                        .getUri())
                .withProcessingTime(0L)
                .withSize(0)
                .withHits(Collections.emptyList());
    }
}
