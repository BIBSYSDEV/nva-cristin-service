package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.common.client.PostApiClient;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.model.Constants.PROJECT_PATH_NVA;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.attempt.Try.attempt;

public class CreateCristinProjectApiClient extends PostApiClient  {

    public CreateCristinProjectApiClient(HttpClient client) {
        super(client);
    }

    /**
     * Used for creating a person in Cristin from the supplied Person object.
     */
    public NvaProject createProjectInCristin(NvaProject nvaProject) throws ApiGatewayException {
        String payload = generatePayloadFromRequest(nvaProject);
        URI uri = getCristinProjectPostUri();
        HttpResponse<String> response = post(uri, payload);
        checkPostHttpStatusCode(getNvaApiUri(PROJECT_PATH_NVA), response.statusCode());

        return createProjectFromResponse(response);
    }

    private String generatePayloadFromRequest(NvaProject nvaProject) {
        CristinProject  cristinProject = nvaProject.toCristinProject();
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(cristinProject)).orElseThrow();
    }

    private URI getCristinProjectPostUri() {
        return new UriWrapper(CRISTIN_API_URL).addChild(PROJECTS_PATH).getUri();
    }

    private NvaProject createProjectFromResponse(HttpResponse<String> response) throws BadGatewayException {
        CristinProject cristinProject = getDeserializedResponse(response, CristinProject.class);
        NvaProject nvaProject = cristinProject.toNvaProject();
        nvaProject.setContext(NvaProject.PROJECT_CONTEXT);
        return nvaProject;
    }
}
