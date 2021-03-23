package no.unit.nva.cristin.projects;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

public class FetchOneCristinProject extends ApiGatewayHandler<Void, NvaProject> {

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchOneCristinProject() {
        super(Void.class, new Environment());
    }

    @Override
    protected NvaProject processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, NvaProject output) {
        return HttpURLConnection.HTTP_OK;
    }
}
