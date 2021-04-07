package no.unit.nva.cristin.projects;

import java.net.HttpURLConnection;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class BadGatewayException extends ApiGatewayException {

    public BadGatewayException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_BAD_GATEWAY;
    }
}
