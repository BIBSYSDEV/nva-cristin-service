package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.LANGUAGE;
import static no.unit.nva.cristin.projects.Constants.TITLE;
import static no.unit.nva.cristin.projects.ErrorMessages.ERROR_MESSAGE_TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS;
import com.amazonaws.services.lambda.runtime.Context;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

/**
 * Handler for requests to Lambda function.
 */
public class FetchCristinProjects extends CristinHandler<Void, ProjectsWrapper> {

    private static final char CHARACTER_DASH = '-';
    private static final char CHARACTER_COMMA = ',';
    private static final char CHARACTER_PERIOD = '.';

    private final transient CristinApiClient cristinApiClient;

    @SuppressWarnings("unused")
    @JacocoGenerated
    public FetchCristinProjects() {
        this(new Environment());
    }

    @JacocoGenerated
    public FetchCristinProjects(Environment environment) {
        this(new CristinApiClient(), environment);
    }

    protected FetchCristinProjects(CristinApiClient cristinApiClient, Environment environment) {
        super(Void.class, environment);
        this.cristinApiClient = cristinApiClient;
    }

    @Override
    protected ProjectsWrapper processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        String language = getValidLanguage(requestInfo);
        String title = getValidTitle(requestInfo);

        return getTransformedCristinProjectsUsingWrapperObject(language, title);
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, ProjectsWrapper output) {
        return HttpURLConnection.HTTP_OK;
    }

    private String getValidTitle(RequestInfo requestInfo) throws BadRequestException {
        return getQueryParam(requestInfo, TITLE)
            .filter(this::isValidTitle)
            .orElseThrow(() -> new BadRequestException(ERROR_MESSAGE_TITLE_MISSING_OR_HAS_ILLEGAL_CHARACTERS));
    }

    private ProjectsWrapper getTransformedCristinProjectsUsingWrapperObject(String language, String title)
        throws ApiGatewayException {

        Map<String, String> requestQueryParams = new ConcurrentHashMap<>();
        requestQueryParams.put(TITLE, title);
        requestQueryParams.put(LANGUAGE, language);

        return cristinApiClient.queryCristinProjectsIntoWrapperObjectWithAdditionalMetadata(requestQueryParams);
    }

    private boolean isValidTitle(String str) {
        char[] charArray = str.toCharArray();
        for (char c : charArray) {
            if (!isValidCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidCharacter(char c) {
        return Character.isWhitespace(c)
            || Character.isLetterOrDigit(c)
            || c == CHARACTER_DASH
            || c == CHARACTER_COMMA
            || c == CHARACTER_PERIOD;
    }

}
