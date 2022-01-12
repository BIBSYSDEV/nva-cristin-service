package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.person.client.AuthorizedCristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;

import static no.unit.nva.cristin.common.client.CristinAuthenticator.getHttpClient;

public class AuthorizedQueryPersonHandler extends ApiGatewayHandler<Void, Person> {

    public static final String ERROR_MESSAGE_INVALID_PARAMETER_FOR_PERSON_ID =
            "Invalid query parameter for national identifier, needs to be a number with 11 digits ";
    public static final int NATIONAL_IDENTIFIER_LENGTH = 11;
    private static final String NationalIdentifierNumber = "nin";


    @JacocoGenerated
    public AuthorizedQueryPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public AuthorizedQueryPersonHandler(Environment environment) {
        super(Void.class, environment);
    }

    private static boolean isPositiveLong(String str) {
        try {
            long value = Long.parseLong(str);
            return value > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected Person processInput(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        String identifier = getValidId(requestInfo);
        return getAuthorizedCristinPersonApiClient().getCristinPerson(identifier).toPerson();
    }

    protected AuthorizedCristinPersonApiClient getAuthorizedCristinPersonApiClient() {
        return new AuthorizedCristinPersonApiClient(getHttpClient());
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
        String nin = requestInfo.getQueryParameter(NationalIdentifierNumber);
        if (isValidIdentifier(nin)) {
            return nin;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PARAMETER_FOR_PERSON_ID);
    }

    private boolean isValidIdentifier(String identifier) {
        return isPositiveLong(identifier) && identifier.length() == NATIONAL_IDENTIFIER_LENGTH;
    }

}
