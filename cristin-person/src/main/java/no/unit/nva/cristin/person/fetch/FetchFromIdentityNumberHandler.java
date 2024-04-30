package no.unit.nva.cristin.person.fetch;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.URI;
import java.util.Optional;
import no.bekk.bekkopen.person.FodselsnummerValidator;
import no.unit.nva.cristin.person.client.CristinPersonApiClient;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP;
import static no.unit.nva.cristin.common.client.CristinAuthenticator.getHttpClient;
import static no.unit.nva.utils.AccessUtils.clientIsCustomerAdministrator;
import static no.unit.nva.utils.AccessUtils.requesterIsUserAdministrator;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;

@JacocoGenerated
public class FetchFromIdentityNumberHandler extends ApiGatewayHandler<TypedValue, Person> {

    private static final Logger logger = LoggerFactory.getLogger(FetchFromIdentityNumberHandler.class);

    public static final String NIN_TYPE = "NationalIdentificationNumber";
    public static final String NOT_AUTHORIZED_TO_USE_THIS_ENDPOINT = "CLient is not authorized to use this endpoint";
    public static final String FOUND_THE_FOLLOWING_RESOURCE = "Client found the following resource: {}";

    private final transient CristinPersonApiClient apiClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public FetchFromIdentityNumberHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchFromIdentityNumberHandler(Environment environment) {
        this(new CristinPersonApiClient(getHttpClient()), environment);
    }

    public FetchFromIdentityNumberHandler(CristinPersonApiClient apiClient, Environment environment) {
        super(TypedValue.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Person processInput(TypedValue input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

        if (!clientIsAuthorized(requestInfo)) {
            logger.info(NOT_AUTHORIZED_TO_USE_THIS_ENDPOINT);
            throw new ForbiddenException();
        }

        validateQueryParameters(requestInfo);
        validateInput(input);

        return apiClient.getPersonFromNationalIdentityNumber(input.getValue());
    }

    @Override
    protected Integer getSuccessStatusCode(TypedValue input, Person output) {
        var personCristinId = getPotentialPersonId(output);
        logger.info(FOUND_THE_FOLLOWING_RESOURCE, personCristinId);
        return HttpURLConnection.HTTP_OK;
    }

    private static String getPotentialPersonId(Person output) {
        return Optional.ofNullable(output)
                   .map(Person::id)
                   .map(URI::toString)
                   .orElse(null);
    }

    private boolean clientIsAuthorized(RequestInfo requestInfo) {
        return requesterIsUserAdministrator(requestInfo) || clientIsCustomerAdministrator(requestInfo);
    }

    private void validateQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        if (!requestInfo.getQueryParameters().keySet().isEmpty()) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP);
        }
    }

    private void validateInput(TypedValue input) throws BadRequestException {
        if (Objects.nonNull(input)
            && NIN_TYPE.equals(input.getType())
            && Objects.nonNull(input.getValue())
            && isValidNationalIdentificationNumber(input.getValue())) {

            return;
        }
        throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
    }

    private boolean isValidNationalIdentificationNumber(String nationalIdentificationNumber) {
        return FodselsnummerValidator.isValid(nationalIdentificationNumber);
    }
}
