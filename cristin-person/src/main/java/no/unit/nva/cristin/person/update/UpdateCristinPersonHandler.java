package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.client.PatchApiClient.EMPTY_JSON;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static nva.commons.core.attempt.Try.attempt;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.HttpURLConnection;
import no.unit.nva.utils.AccessUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class UpdateCristinPersonHandler extends ApiGatewayHandler<String, String> {

    @SuppressWarnings("unused")
    @JacocoGenerated
    public UpdateCristinPersonHandler() {
        this(new Environment());
    }

    public UpdateCristinPersonHandler(Environment environment) {
        super(String.class, environment);
    }

    @Override
    protected String processInput(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        validateHasAccessRights(requestInfo);

        ObjectNode objectNode = readJsonFromInput(input);
        PersonPatchValidator.validate(objectNode);

        return EMPTY_JSON;
    }

    @Override
    protected Integer getSuccessStatusCode(String input, String output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    private void validateHasAccessRights(RequestInfo requestInfo) throws ForbiddenException {
        if (!AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            throw new ForbiddenException();
        }
    }

    private ObjectNode readJsonFromInput(String input) throws BadRequestException {
        return attempt(() -> (ObjectNode) OBJECT_MAPPER.readTree(input))
            .orElseThrow(fail -> new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD));
    }
}
