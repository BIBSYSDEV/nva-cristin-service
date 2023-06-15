package no.unit.nva.cristin.projects.update;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.net.MediaType;
import java.net.http.HttpClient;
import java.util.Map;
import no.unit.nva.Validator;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.projects.fetch.FetchCristinProjectApiClient;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.utils.HandlerAccessCheck;
import no.unit.nva.utils.ResourceAccessCheck;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.unit.nva.cristin.common.Utils.getValidIdentifier;
import static no.unit.nva.cristin.common.Utils.readJsonFromInput;
import static no.unit.nva.cristin.model.Constants.DEFAULT_RESPONSE_MEDIA_TYPES;
import static no.unit.nva.cristin.projects.update.UpdateProjectResourceAccessCheck.USER_IDENTIFIER;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;

public class UpdateCristinProjectHandler extends ApiGatewayHandler<String, Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpdateCristinProjectHandler.class);

    public static final String ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD =
            "No supported fields in payload, not doing anything";
    public static final String ACCESS_RIGHT_OR_PROJECT_OWNERSHIP_ALLOWING_UPDATE = "User has required rights by "
                                                                                   + "either access right or project "
                                                                                   + "ownership, allowing update";

    private final transient UpdateCristinProjectApiClient cristinApiClient;
    private final transient FetchCristinProjectApiClient fetchApiClient;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public UpdateCristinProjectHandler() {
        this(CristinAuthenticator.getHttpClient(), new Environment());
    }

    @JacocoGenerated
    public UpdateCristinProjectHandler(HttpClient httpClient, Environment environment) {
        this(new UpdateCristinProjectApiClient(httpClient),
             new FetchCristinProjectApiClient(httpClient), environment);
    }

    /**
     * Creating the handler instance with required parameters.
     */
    public UpdateCristinProjectHandler(UpdateCristinProjectApiClient cristinApiClient,
                                       FetchCristinProjectApiClient fetchApiClient, Environment environment) {
        super(String.class, environment);
        this.cristinApiClient = cristinApiClient;
        this.fetchApiClient = fetchApiClient;
    }

    @Override
    protected Void processInput(String input, RequestInfo requestInfo, Context context) throws ApiGatewayException {

        HandlerAccessCheck handlerAccessCheck = new UpdateProjectHandlerAccessCheck();
        handlerAccessCheck.verifyAccess(requestInfo);

        if (!handlerAccessCheck.isVerified()) {
            var projectFromUpstream =
                fetchApiClient.queryOneCristinProjectUsingIdIntoNvaProject(getValidIdentifier(requestInfo));

            ResourceAccessCheck<NvaProject> resourceAccessCheck = new UpdateProjectResourceAccessCheck();
            resourceAccessCheck.verifyAccess(projectFromUpstream,
                                             Map.of(USER_IDENTIFIER, extractCristinIdentifier(requestInfo)));

            if (!resourceAccessCheck.isVerified()) {
                throw new ForbiddenException();
            }
        }

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

        var objectNode = readJsonFromInput(input);
        Validator<ObjectNode> validator = new ProjectPatchValidator();
        validator.validate(objectNode);
        var cristinJson = new CristinProjectPatchJsonCreator(objectNode).create().getOutput();

        if (noSupportedValuesPresent(cristinJson)) {
            throw new BadRequestException(ERROR_MESSAGE_NO_SUPPORTED_FIELDS_IN_PAYLOAD);
        }

        logger.info(ACCESS_RIGHT_OR_PROJECT_OWNERSHIP_ALLOWING_UPDATE);

        return cristinApiClient.updateProjectInCristin(getValidIdentifier(requestInfo), cristinJson);
    }

    @Override
    protected Integer getSuccessStatusCode(String input, Void output) {
        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    @Override
    protected List<MediaType> listSupportedMediaTypes() {
        return DEFAULT_RESPONSE_MEDIA_TYPES;
    }

    private boolean noSupportedValuesPresent(ObjectNode cristinJson) {
        return cristinJson.isEmpty();
    }

}
