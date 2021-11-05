package no.unit.nva.cristin.organization;

import com.fasterxml.jackson.databind.JsonNode;
import no.unit.nva.cristin.organization.exception.FailedHttpRequestException;
import no.unit.nva.cristin.organization.exception.HttpClientFailureException;
import no.unit.nva.cristin.organization.exception.InvalidUriException;
import no.unit.nva.cristin.organization.exception.NonExistingUnitError;
import no.unit.nva.cristin.organization.utils.Language;
import org.apache.http.HttpStatus;

import java.net.URI;
import java.net.http.HttpResponse;

import static java.util.Objects.isNull;

public abstract class HttpExecutor {

    public static final String ERROR_MESSAGE_FORMAT = "%d:%s";
    public static int FIRST_NON_SUCCESSFUL_CODE = HttpStatus.SC_MULTIPLE_CHOICES;
    public static int FIRST_SUCCESSFUL_CODE = HttpStatus.SC_OK;
    public static String NULL_HTTP_RESPONSE_ERROR_MESSAGE = "No HttpResponse found";

    public abstract InstitutionListResponse getInstitutions(Language language) throws HttpClientFailureException;

    public abstract JsonNode getNestedInstitution(URI uri, Language language)
        throws HttpClientFailureException, InvalidUriException, FailedHttpRequestException;

    public abstract JsonNode getSingleUnit(URI uri, Language language)
        throws NonExistingUnitError, HttpClientFailureException, InterruptedException;

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
