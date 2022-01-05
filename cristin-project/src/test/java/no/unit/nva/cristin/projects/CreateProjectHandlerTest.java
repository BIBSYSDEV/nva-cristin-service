package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.model.Organization;
import no.unit.nva.testutils.HandlerRequestBuilder;
import no.unit.nva.utils.HttpUtils;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.MediaTypes;
import nva.commons.core.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static com.google.common.net.HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static java.net.HttpURLConnection.HTTP_OK;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CreateProjectHandlerTest {

    CreateProjectHandler createProjectHandler;
    private static final ObjectMapper restApiMapper = JsonUtils.dtoObjectMapper;
    private ByteArrayOutputStream output;
    private Context context;


    @BeforeEach
    void setUp() {
        createProjectHandler = new CreateProjectHandler(new HttpUtils().getBasicAuthenticator());
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
    }

    @Test
    void createSimpleProjectAndReturnNewId() throws IOException {
        final NvaProject expectedProject = getNvaProject();
        InputStream inputStream = generateHandlerRequest(expectedProject);
        createProjectHandler.handleRequest(inputStream, output, context);
        GatewayResponse<NvaProject> gatewayResponse = GatewayResponse.fromOutputStream(output);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));
        NvaProject actualProject = gatewayResponse.getBodyObject(NvaProject.class);
        assertEquals(expectedProject, actualProject);
    }

    @Test
    void checkHandlerValidateThrowsAPIGateWayException() throws IOException {
        final NvaProject nvaProject = getNvaProject();
        nvaProject.setTitle(null);
        InputStream inputStream = generateHandlerRequest(nvaProject);
        createProjectHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Organization> gatewayResponse = GatewayResponse.fromOutputStream(output);

        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, gatewayResponse.getStatusCode());
//        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
//        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

    }

    @Test
    void checkHandlerCanAccessSecret() throws IOException {
        final NvaProject nvaProject = getNvaProject();
        InputStream inputStream = generateHandlerRequest(nvaProject);
        createProjectHandler.handleRequest(inputStream, output, context);
        GatewayResponse<Organization> gatewayResponse = GatewayResponse.fromOutputStream(output);

        assertEquals(HTTP_OK, gatewayResponse.getStatusCode());
//        assertThat(gatewayResponse.getHeaders(), hasKey(CONTENT_TYPE));
//        assertThat(gatewayResponse.getHeaders(), hasKey(ACCESS_CONTROL_ALLOW_ORIGIN));

    }


    private InputStream generateHandlerRequest(NvaProject project) throws JsonProcessingException {
        Map<String, String> headers = Map.of(CONTENT_TYPE, MediaTypes.APPLICATION_JSON_LD.type());
        return new HandlerRequestBuilder<NvaProject>(restApiMapper)
                .withHeaders(headers)
                .withBody(project)
                .build();
    }

    private NvaProject getNvaProject() {
        NvaProject project = new NvaProject();
        project.setTitle(randomString());
        project.setContributors(Collections.emptyList());
        project.setCoordinatingInstitution(new Organization.Builder().build());
        project.setStartDate(Instant.now());
        return project;
    }

}
