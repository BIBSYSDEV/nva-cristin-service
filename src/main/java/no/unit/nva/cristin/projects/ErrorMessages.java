package no.unit.nva.cristin.projects;

import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class ErrorMessages {

    public static final String ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID =
        "Error fetching cristin project with id: %s . Exception Message: %s";
    public static final String ERROR_MESSAGE_BACKEND_FETCH_FAILED =
        "Your request cannot be processed at this time due to an upstream error";
    public static final String ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID =
        "Project matching id %s does not have valid data";
    public static final String ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED =
        "Query failed from params: %s with exception: %s";
    public static final String ERROR_MESSAGE_READING_RESPONSE_FAIL =
        "Error when reading response with body: %s, causing exception: %s";
    public static final String ERROR_MESSAGE_SERVER_ERROR =
        "Internal server error. Contact application administrator.";
    public static final String ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID =
        "Invalid path parameter for id, needs to be a number";
    public static final String ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS =
        "Parameter 'query' is missing or invalid. "
            + "May only contain alphanumeric characters, dash, comma, period and whitespace";
    public static final String ERROR_MESSAGE_LANGUAGE_INVALID = "Parameter 'language' has invalid value";
    public static final String ERROR_MESSAGE_PAGE_VALUE_INVALID = "Parameter 'page' has invalid value";
    public static final String ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE =
        "Remote service responded with status: %s when client called uri: %s";
    public static final String ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID = "Parameter 'results' has invalid value";
    public static final String ERROR_MESSAGE_PAGE_OUT_OF_SCOPE =
        "Page requested is out of scope. Query contains %s results";
    public static final String ERROR_MESSAGE_UNACCEPTABLE_CONTENT_TYPE =
        "Your request cannot be processed because the supplied content-type '%s' cannot be understood";
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_SEARCH =
        "Invalid query param supplied. Valid ones are 'query', 'page', 'results' and 'language'";
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMS_ON_LOOKUP =
        "Invalid query param supplied. Valid one are 'language'";
}
