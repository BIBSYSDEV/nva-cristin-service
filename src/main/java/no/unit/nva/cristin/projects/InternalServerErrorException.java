package no.unit.nva.cristin.projects;

import java.net.HttpURLConnection;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class InternalServerErrorException extends ApiGatewayException {

    public InternalServerErrorException(String message) {
        super(message);
    }

    @Override
    protected Integer statusCode() {
        return HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}
