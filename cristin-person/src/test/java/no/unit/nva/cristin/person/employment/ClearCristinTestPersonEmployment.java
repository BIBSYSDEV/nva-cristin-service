package no.unit.nva.cristin.person.employment;

import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.employment.delete.DeletePersonEmploymentClient;
import no.unit.nva.cristin.person.employment.query.QueryPersonEmploymentClient;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClearCristinTestPersonEmployment {

    private static final Logger logger = LoggerFactory.getLogger(ClearCristinTestPersonEmployment.class);

    /**
     * Deletes all employments for a given person in cristin.
     * @param personId cristin identifier of person to delete employment for
     * @throws ApiGatewayException an error occurred during execution
     */
    public static void clearEmployment(String personId) throws ApiGatewayException {
        SearchResponse<CristinPersonEmployment>  queryResponse =
                new QueryPersonEmploymentClient(CristinAuthenticator.getHttpClient()).generateQueryResponse(personId);
        List<CristinPersonEmployment> hits = (List<CristinPersonEmployment>) queryResponse.getHits();
        hits.forEach(hit -> deleteEmployment(personId, hit));
    }

    private static void deleteEmployment(String personId, CristinPersonEmployment cristinPersonEmployment) {
        try {
            new DeletePersonEmploymentClient(CristinAuthenticator.getHttpClient())
                    .deletePersonEmployment(personId, cristinPersonEmployment.getId());
            logger.info("Deleted employment {} for person {}", cristinPersonEmployment.getId(), personId);
        } catch (ApiGatewayException e) {
            logger.error("Exception deleting employment for person with id '{}'", personId, e);
        }
    }

}
