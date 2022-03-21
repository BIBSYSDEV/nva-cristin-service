package no.unit.nva.cristin.common;

import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class ErrorMessages {

    public static final String ERROR_MESSAGE_FETCHING_CRISTIN_PROJECT_WITH_ID =
        "Error fetching cristin project with id: %s . Exception Message: %s";
    public static final String ERROR_MESSAGE_BACKEND_FETCH_FAILED =
        "The request failed because of a problem with the upstream server";
    public static final String ERROR_MESSAGE_CRISTIN_PROJECT_MATCHING_ID_IS_NOT_VALID =
        "Project matching id %s does not have valid data";
    public static final String ERROR_MESSAGE_QUERY_WITH_PARAMS_FAILED =
        "Query failed from params: %s with exception: %s";
    public static final String ERROR_MESSAGE_READING_RESPONSE_FAIL =
        "Error when reading response with body: %s, causing exception: %s";
    public static final String ERROR_MESSAGE_SERVER_ERROR =
        "Internal server error. Contact application administrator.";
    public static final String ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID =
        "Invalid path parameter for identifier, needs to be a number";
    public static final String ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_PERSON_ID =
        "Invalid path parameter for identifier, needs to be a number or an ORCID";
    public static final String ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_ID_FOUR_NUMBERS =
        "Invalid path parameter for identifier,"
            + " needs to be organization identifier matching pattern /(?:\\d+.){3}\\d+/, e.g. (100.0.0.0)";
    public static final String ERROR_MESSAGE_QUERY_MISSING_OR_HAS_ILLEGAL_CHARACTERS =
        "Parameter 'query' is missing or invalid. "
            + "May only contain alphanumeric characters, dash, comma, period and whitespace";
    public static final String ERROR_MESSAGE_NAME_MISSING_OR_HAS_ILLEGAL_CHARACTERS =
            "Parameter 'name' is missing or invalid. "
                    + "May only contain alphanumeric characters, dash, comma, period and whitespace";
    public static final String ERROR_MESSAGE_DEPTH_INVALID = "Parameter 'depth' has invalid value. "
            + "Must be 'top' or 'full'";
    public static final String ERROR_MESSAGE_LANGUAGE_INVALID = "Parameter 'language' has invalid value";
    public static final String ERROR_MESSAGE_PAGE_VALUE_INVALID = "Parameter 'page' has invalid value";
    public static final String ERROR_MESSAGE_BACKEND_FAILED_WITH_STATUSCODE =
        "Remote service responded with status: %s when client called uri: %s";
    public static final String ERROR_MESSAGE_NUMBER_OF_RESULTS_VALUE_INVALID = "Parameter 'results' has invalid value";
    public static final String ERROR_MESSAGE_PAGE_OUT_OF_SCOPE =
        "Page requested is out of scope. Query contains %s results";
    public static final String ERROR_MESSAGE_UNSUPPORTED_CONTENT_TYPE =
        "%s contains no supported Accept header values. Supported values are: application/json; charset=utf-8, "
            + "application/ld+json";
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMETERS_ON_SEARCH =
        "Invalid query parameter supplied. Valid parameters are 'query', 'page', 'results' and 'language'";
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMETERS_ON_PERSON_SEARCH =
            "Invalid query parameter supplied. Valid parameters are 'name', 'page', 'results' and 'language'";
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_LOOKUP =
        "Invalid query parameter supplied. Valid parameter is 'language'";
    public static final String ERROR_MESSAGE_INVALID_QUERY_PARAMETER_ON_PERSON_LOOKUP =
        "This endpoint does not support query parameters";
    public static final String ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI =
        "The requested resource '%s' was not found";
    public static final String ERROR_MESSAGE_INVALID_PAYLOAD = "Supplied payload is not valid";
    public static final String ERROR_MESSAGE_INVALID_PERSON_ID = "Invalid path parameter for person id";

}
