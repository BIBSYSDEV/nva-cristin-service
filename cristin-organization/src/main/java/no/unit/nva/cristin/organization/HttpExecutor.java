package no.unit.nva.cristin.organization;

import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.exception.FailedHttpRequestException;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.NotFoundException;

import java.net.URI;
import java.net.http.HttpResponse;

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Objects.isNull;

public abstract class HttpExecutor {

    public static final String ERROR_MESSAGE_FORMAT = "%d:%s";
    public static int FIRST_NON_SUCCESSFUL_CODE = HTTP_MULT_CHOICE;
    public static int FIRST_SUCCESSFUL_CODE = HTTP_OK;
    public static String NULL_HTTP_RESPONSE_ERROR_MESSAGE = "No HttpResponse found";

    public abstract SearchResponse<Organization> getInstitutions() throws FailedHttpRequestException;

    public abstract Organization getSingleUnit(URI uri)
            throws NotFoundException, FailedHttpRequestException, InterruptedException;

    protected HttpResponse<String> throwExceptionIfNotSuccessful(HttpResponse<String> response)
        throws FailedHttpRequestException {
        if (isNull(response)) {
            throw new FailedHttpRequestException(NULL_HTTP_RESPONSE_ERROR_MESSAGE);
        } else if (response.statusCode() >= FIRST_SUCCESSFUL_CODE
            && response.statusCode() < FIRST_NON_SUCCESSFUL_CODE) {
            return response;
        } else {
            throw new FailedHttpRequestException(errorMessage(response));
        }
    }

    private String errorMessage(HttpResponse<String> response) {
        return String.format(ERROR_MESSAGE_FORMAT, response.statusCode(), response.body());
    }
}
