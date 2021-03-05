package no.unit.nva.cristin.projects;

import java.net.HttpURLConnection;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.JacocoGenerated;

public class BadRequestException extends ApiGatewayException {

    @JacocoGenerated
    public BadRequestException(String message) {
        super(message);
    }

    @JacocoGenerated
    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_BAD_REQUEST;
    }
}
