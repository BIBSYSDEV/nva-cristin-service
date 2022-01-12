package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Set;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID;
import static no.unit.nva.cristin.common.client.CristinAuthenticator.getHttpClient;
import static no.unit.nva.cristin.person.handler.AuthorizedQueryPersonHandler.NATIONAL_IDENTIFIER_LENGTH;

public class AuthorizedGetPersonHandler extends ApiGatewayHandler<Void, Person> {

    private static final String NationalIdentifierNumber = "nin";
    private static final String READ_NATIONAL_IDENTIFICATION_NUMBER = "READ_NATIONAL_IDENTIFICATION_NUMBER";

    @JacocoGenerated
    public AuthorizedGetPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public AuthorizedGetPersonHandler(Environment environment) {
        super(Void.class, environment);
    }


    @Override
    protected Person processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        if (requesterHasAccess(requestInfo.getAccessRights())) {
            String identifier = getValidId(requestInfo);
            return new CristinPersonApiClient(getHttpClient()).generateGetResponse(identifier);
        } else {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID);
        }
    }

    private boolean requesterHasAccess(Set<String> accessRights) {
        return accessRights.contains(READ_NATIONAL_IDENTIFICATION_NUMBER);
    }

    /**
     * Define the success status code.
     *
     * @param input  The request input.
     * @param output The response output
     * @return the success status code.
     */
    @Override
    protected Integer getSuccessStatusCode(Void input, Person output) {
        return HttpURLConnection.HTTP_OK;
    }

    private String getValidId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = requestInfo.getPathParameter(NationalIdentifierNumber);
        if (isValidIdentifier(identifier)) {
            return identifier;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID);
    }

    private boolean isValidIdentifier(String identifier) {
        return Utils.isPositiveInteger(identifier) && identifier.length() == NATIONAL_IDENTIFIER_LENGTH;
    }

}
