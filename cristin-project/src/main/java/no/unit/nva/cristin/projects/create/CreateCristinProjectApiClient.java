package no.unit.nva.cristin.projects.create;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.cristin.projects.model.cristin.CristinProjectBuilder.removeFieldsNotSupportedByPost;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.common.client.PostApiClient;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CreateCristinProjectApiClient extends PostApiClient {

    public static final ObjectMapper OBJECT_MAPPER_NON_EMPTY = JsonUtils.dynamoObjectMapper;

    public CreateCristinProjectApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Used for creating a person in Cristin from the supplied Person object.
     */
    public NvaProject createProjectInCristin(NvaProject nvaProject) throws ApiGatewayException {
        var payload = generatePayloadFromRequest(nvaProject);
        var uri = getCristinProjectPostUri();
        var response = post(uri, payload);
        checkPostHttpStatusCode(getNvaApiUri(PROJECT_PATH_NVA), response.statusCode(), response.body());
        return createProjectFromResponse(response);
    }

    private String generatePayloadFromRequest(NvaProject nvaProject) {
        var cristinProject = nvaProject.toCristinProject();
        removeFieldsNotSupportedByPost(cristinProject);
        return attempt(() -> OBJECT_MAPPER_NON_EMPTY.writeValueAsString(cristinProject)).orElseThrow();
    }

    private URI getCristinProjectPostUri() {
        return UriWrapper.fromUri(CRISTIN_API_URL).addChild(PROJECTS_PATH).getUri();
    }

    private NvaProject createProjectFromResponse(HttpResponse<String> response) throws BadGatewayException {
        var cristinProject = getDeserializedResponse(response, CristinProject.class);
        var nvaProject = cristinProject.toNvaProject();
        nvaProject.setContext(NvaProject.PROJECT_CONTEXT);
        return nvaProject;
    }
}
