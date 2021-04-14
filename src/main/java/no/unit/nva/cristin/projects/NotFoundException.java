package no.unit.nva.cristin.projects;

import java.net.HttpURLConnection;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class NotFoundException extends ApiGatewayException {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_NOT_FOUND;
    }
}
