package no.unit.nva.cristin.person.update;

import static java.util.Objects.isNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class UpdateCristinPersonHandler extends ApiGatewayHandler<Person, String> {

    @SuppressWarnings("unused")
    @JacocoGenerated
    public UpdateCristinPersonHandler() {
        this(new Environment());
    }

    public UpdateCristinPersonHandler(Environment environment) {
        super(Person.class, environment);
    }

    @Override
    protected String processInput(Person input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        validateHasAccessRights(requestInfo);

        validateContainsPayload(input);

        return EMPTY_JSON;
    }

    @Override
    protected Integer getSuccessStatusCode(Person input, String output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            throw new ForbiddenException();
        }
    }

    private void validateContainsPayload(Person input) throws BadRequestException {
        if (isNull(input)) {
            throw new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD);
        }
    }
}
