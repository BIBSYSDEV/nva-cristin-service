package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.common.model.SearchResponse;
import no.unit.nva.cristin.model.nva.Organization;
import no.unit.nva.cristin.organization.exception.HttpClientFailureException;
import no.unit.nva.cristin.organization.exception.NonExistingUnitError;
import no.unit.nva.cristin.organization.utils.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

public class CristinApiClient {

    private static final Logger logger = LoggerFactory.getLogger(CristinApiClient.class);

    private final transient HttpExecutor httpExecutor;

    public CristinApiClient() {
        this.httpExecutor = new HttpExecutorImpl();
    }


    /**
     * Get information for a Organization.
     *
     * @param uri      the Cristin unit URI
     * @param language a language code for the details of each unit
     * @return an {@link Organization} containing the information
     * @throws InterruptedException       when the http client throws an {@link InterruptedException } exception
     * @throws NonExistingUnitError       when the URI does not correspond to an existing unit.
     * @throws HttpClientFailureException when Cristin server reports failure
     */
    public Organization getSingleUnit(URI uri, Language language)
            throws InterruptedException, NonExistingUnitError, HttpClientFailureException {
        logger.info("Fetching results for: " + uri.toString());
        Organization result = httpExecutor.getSingleUnit(uri, language);
        return result;
    }


    public SearchResponse<Organization> queryInstitutions(Map<String, String> requestQueryParams)
            throws HttpClientFailureException {
        return new SearchResponse()
                .withProcessingTime(0L)
                .withHits(Collections.emptyList());
    }

}
