package no.unit.nva.cristin.person.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.unit.nva.cristin.person.client.AuthorizedCristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.Objects;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.common.client.CristinAuthenticator.getHttpClient;

@JacocoGenerated
@SuppressWarnings("unused")
public class FetchFromIdentityNumberHandler extends ApiGatewayHandler<TypedValue, Person> {
    private static final Logger logger = LoggerFactory.getLogger(FetchFromIdentityNumberHandler.class);

    public static final String NIN_TYPE = "NationalIdentificationNumber";
    public static final String ERROR_MESSAGE_INVALID_PAYLOAD = "Invalid payload in body";
    private static final String READ_NATIONAL_IDENTIFICATION_NUMBER = "READ_NATIONAL_IDENTIFICATION_NUMBER";  // Replace with real AccessRight
    private static final String ERROR_MESSAGE_NOT_AUTHORIZED = "Not authorized to use National Identification Number";

    private final transient AuthorizedCristinPersonApiClient apiClient;

    @JacocoGenerated
    public FetchFromIdentityNumberHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchFromIdentityNumberHandler(Environment environment) {
        this(new AuthorizedCristinPersonApiClient(getHttpClient()), environment);
    }

    public FetchFromIdentityNumberHandler(AuthorizedCristinPersonApiClient apiClient, Environment environment) {
        super(TypedValue.class, environment);
        this.apiClient = apiClient;
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    @Override
    protected Person processInput(TypedValue input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateAccess(requestInfo);

        validateQueryParameters(requestInfo);
        try {
            validateInput(input);
        } catch (JsonProcessingException e) {
        }

        return apiClient.getPersonFromNationalIdentityNumber(input.getValue());
    }

    private void validateAccess(RequestInfo requestInfo) throws BadRequestException {
        if (!requesterHasAccessToReadNationalIdentificationNumber(requestInfo)) {
            throw new BadRequestException(ERROR_MESSAGE_NOT_AUTHORIZED);

        }
    }

    private boolean requesterHasAccessToReadNationalIdentificationNumber(RequestInfo requestInfo) {
        return true;
//        return requestInfo.getAccessRights().contains(READ_NATIONAL_IDENTIFICATION_NUMBER);
    }

    @Override
    protected Integer getSuccessStatusCode(TypedValue input, Person output) {
        return HttpURLConnection.HTTP_OK;
    }

    private void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_PERSON_LOOKUP);
        }
    }

    private void validateInput(TypedValue input) throws BadRequestException, JsonProcessingException {
        if (Objects.nonNull(input)
            && NIN_TYPE.equals(input.getType())
            && Objects.nonNull(input.getValue())
            && isValidNationalIdentificationNumber(input.getValue())) {

            return;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
    }

    private boolean isValidNationalIdentificationNumber(String nationalIdentificationNumber) {
        logger.info("isValidNationalIdentificationNumber({})", nationalIdentificationNumber);
        return FodselsnummerValidator.isValid(nationalIdentificationNumber);
    }

}