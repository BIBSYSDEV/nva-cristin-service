package no.unit.nva.cristin.biobank;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import nva.commons.core.ioutils.IoUtils;

public final class CristinBiobankStubs {

    private StubMapping stubMapping;

    public CristinBiobankStubs() {
        // no-op
    }

    public void stubSuccess() {
        var response = IoUtils.stringFromResources(Path.of("success.json"));

        createStubWithResponse(response);
    }

    public void stubMalformedJsonInResponse() {
        var response = "{}";
        createStubWithResponse(response);
    }

    public void resetStub() {
        if (stubMapping != null) {
            WireMock.removeStub(stubMapping);
            stubMapping = null;
        }
    }

    private void createStubWithResponse(String response) {
        if (stubMapping != null) {
            throw new IllegalStateException("reset stubs before creating a new stub!");
        }
        this.stubMapping = stubFor(get("/biobank?lang=en,nb,nn")
                                       .willReturn(aResponse()
                                                       .withBody(response)
                                                       .withHeader("Content-Type",
                                                                   "application/json; charset=UTF-8")
                                                       .withStatus(HttpURLConnection.HTTP_OK)));
    }
}
