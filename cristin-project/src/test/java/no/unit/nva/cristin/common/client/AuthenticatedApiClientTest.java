package no.unit.nva.cristin.common.client;

import nva.commons.core.ioutils.IoUtils;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthenticatedApiClientTest {

    private ApiClient cristinApiClient;

    @BeforeEach
    void setUp() {
        cristinApiClient = new ApiClient(CristinAuthenticator.getHttpClient());
    }

    //    @Test
    void createSimpleProjectAndReturnStatusCreated() {
        String payload = IoUtils.stringFromResources(Path.of("simple_cristin_project.json"));
        URI cristinUri = new UriWrapper(CRISTIN_API_URL).addChild(PROJECTS_PATH).getUri();
        HttpResponse<String> httpResponse  = cristinApiClient.fetchPostResult(cristinUri, payload);
        System.out.println(httpResponse.body());
        assertEquals(HTTP_CREATED, httpResponse.statusCode());
    }

}
