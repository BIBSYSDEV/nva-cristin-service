package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.common.client.PatchApiClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;

import static no.unit.nva.cristin.model.Constants.BASE_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.model.Constants.HTTPS;
import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.utils.UriUtils.PROJECT;

public class UpdateCristinApiClient extends PatchApiClient {

    /**
     * Create a cristin API client with default HTTP client.
     */
    public UpdateCristinApiClient() {
        this(HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build());
    }

    public UpdateCristinApiClient(HttpClient client) {
        super(client);
    }

    /**
     * perform an PATCH operation for a Cristin project.
     * @param projectId identifier for project in Cristin
     * @param cristinJson structure holding patch data
     * @return Void
     * @throws ApiGatewayException when operation fails
     */
    public Void updateProjectInCristin(String projectId, ObjectNode cristinJson) throws ApiGatewayException {
        URI uri = generateCristinUri(projectId);
        HttpResponse<String> response = patch(uri, cristinJson.toString());
        checkPatchHttpStatusCode(generateIdUri(projectId), response.statusCode());
        return null;
    }

    private URI generateCristinUri(String projectId) {
        return UriWrapper.fromUri(CRISTIN_API_URL).addChild(PROJECT).addChild(projectId).getUri();
    }

    private URI generateIdUri(String projectId) {
        return new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(PROJECT_PATH_NVA).addChild(projectId)
                .getUri();
    }
}