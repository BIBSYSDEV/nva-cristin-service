package no.unit.nva.cristin.person.employment;

import static no.unit.nva.cristin.common.Utils.CAN_UPDATE_ANY_INSTITUTION;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.employment.delete.DeletePersonEmploymentClient;
import no.unit.nva.cristin.person.employment.query.QueryPersonEmploymentClient;
import no.unit.nva.cristin.person.model.nva.Employment;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearCristinTestPersonEmployment {

    private static final Logger logger = LoggerFactory.getLogger(ClearCristinTestPersonEmployment.class);
    private static final String ERROR_NOT_VALID_IDENTIFIERS = "Identifier for person: {} and/or employment: {} is not "
        + "valid, aborting deletion";

    /**
     * Deletes all employments for a given person in cristin.
     *
     * @param personId cristin identifier of person to delete employment for
     * @throws ApiGatewayException an error occurred during execution
     */
    public static void clearEmployment(String personId) throws ApiGatewayException {
        var queryResponse =
            new QueryPersonEmploymentClient(CristinAuthenticator.getHttpClient()).generateQueryResponse(personId);
        queryResponse.getHits()
            .forEach(hit -> deleteEmployment(personId, hit));

    }

    private static void deleteEmployment(String personId, Employment employment) {
        String employmentId = extractLastPathElement(employment.getId());
        if (notValidIdentifiers(personId, employmentId)) {
            logger.error(ERROR_NOT_VALID_IDENTIFIERS, personId, employmentId);
            return;
        }
        try {
            new DeletePersonEmploymentClient(CristinAuthenticator.getHttpClient())
                .deletePersonEmployment(personId, employmentId, CAN_UPDATE_ANY_INSTITUTION);
            logger.info("Deleted employment {}", employment.getId());
        } catch (ApiGatewayException e) {
            logger.error("Exception deleting employment for person with id '{}'", personId, e);
        }
    }

    private static boolean notValidIdentifiers(String personId, String employmentId) {
        return !Utils.isPositiveInteger(personId) || !Utils.isPositiveInteger(employmentId);
    }
}
