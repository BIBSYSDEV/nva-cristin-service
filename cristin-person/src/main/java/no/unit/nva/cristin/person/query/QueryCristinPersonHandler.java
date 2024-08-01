package no.unit.nva.cristin.person.query;

import com.amazonaws.services.lambda.runtime.Context;
import java.util.Optional;
import no.unit.nva.client.ClientProvider;
import no.unit.nva.cristin.common.client.CristinAuthorizedQueryClient;
import no.unit.nva.cristin.common.handler.CristinQueryHandler;
import no.unit.nva.cristin.model.SearchResponse;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE;
import static no.unit.nva.cristin.common.ErrorMessages.invalidQueryParametersMessage;
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.Constants.SORT;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.query.CristinFacetParamKey.INSTITUTION_PARAM;
import static no.unit.nva.cristin.model.query.CristinFacetParamKey.SECTOR_PARAM;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.VERIFIED;
import static no.unit.nva.utils.AccessUtils.DOING_AUTHORIZED_REQUEST;
import static no.unit.nva.utils.AccessUtils.clientIsCustomerAdministrator;
import static no.unit.nva.utils.AccessUtils.requesterIsUserAdministrator;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import static no.unit.nva.utils.VersioningUtils.ACCEPT_HEADER_KEY_NAME;
import static no.unit.nva.utils.VersioningUtils.extractVersionFromRequestInfo;

public class QueryCristinPersonHandler extends CristinQueryHandler<Void, SearchResponse<Person>> {

    private static final Logger logger = LoggerFactory.getLogger(QueryCristinPersonHandler.class);

    private static final Set<String> VALID_QUERY_PARAMETERS = Set.of(NAME,
                                                                     ORGANIZATION,
                                                                     PAGE,
                                                                     NUMBER_OF_RESULTS,
                                                                     VERIFIED,
                                                                     SECTOR_PARAM.getNvaKey(),
                                                                     INSTITUTION_PARAM.getNvaKey(),
                                                                     SORT);
    private final transient ClientProvider<CristinAuthorizedQueryClient<Map<String, String>, Person>> clientProvider;

    @JacocoGenerated
    @SuppressWarnings("unused")
    public QueryCristinPersonHandler() {
        this(new Environment());
    }

    @JacocoGenerated
    public QueryCristinPersonHandler(Environment environment) {
        this(new DefaultPersonQueryClientProvider(), environment);
    }

    public QueryCristinPersonHandler(
        ClientProvider<CristinAuthorizedQueryClient<Map<String, String>, Person>> clientProvider,
        Environment environment
    ) {
        super(Void.class, environment);
        this.clientProvider = clientProvider;
    }

    @Override
    protected void validateRequest(Void input, RequestInfo requestInfo, Context context) throws ApiGatewayException {
        validateQueryParameterKeys(requestInfo);
    }

    @Override
    protected SearchResponse<Person> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var requestQueryParameters = extractQueryParameters(requestInfo);
        var apiVersion = getApiVersion(requestInfo);
        var apiClient = clientProvider.getClient(apiVersion);

        if (clientIsAuthorized(requestInfo)) {
            logger.info(DOING_AUTHORIZED_REQUEST);
            logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));

            return apiClient.executeAuthorizedQuery(requestQueryParameters);
        } else {
            return apiClient.executeQuery(requestQueryParameters);
        }
    }

    @Override
    protected Integer getSuccessStatusCode(Void input, SearchResponse<Person> output) {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void validateQueryParameterKeys(RequestInfo requestInfo) throws BadRequestException {
        if (!VALID_QUERY_PARAMETERS.containsAll(requestInfo.getQueryParameters().keySet())) {
            throw new BadRequestException(validQueryParameterNamesMessage(VALID_QUERY_PARAMETERS));
        }
    }

    private Map<String, String> extractQueryParameters(RequestInfo requestInfo) throws BadRequestException {
        return buildParametersMap(getValidName(requestInfo),
                                  getValidPage(requestInfo),
                                  getValidNumberOfResults(requestInfo),
                                  extractOrganization(requestInfo),
                                  extractVerified(requestInfo),
                                  extractSectorFacet(requestInfo),
                                  extractOrganizationFacet(requestInfo),
                                  getSort(requestInfo));
    }

    @Override
    protected String getValidName(RequestInfo requestInfo) throws BadRequestException {
        var name = requestInfo.getQueryParameterOpt(NAME);

        if (name.isEmpty()) {
            return null;
        }

        return name.filter(this::isValidQueryString)
                   .orElseThrow(QueryCristinPersonHandler::invalidNameException);
    }

    private static BadRequestException invalidNameException() {
        return new BadRequestException(
            invalidQueryParametersMessage(NAME, ALPHANUMERIC_CHARACTERS_DASH_COMMA_PERIOD_AND_WHITESPACE));
    }

    private String extractOrganization(RequestInfo requestInfo) {
        return getValidOrganization(requestInfo).orElse(null);
    }

    private String extractVerified(RequestInfo requestInfo) {
        return getValidVerified(requestInfo).orElse(null);
    }

    private Optional<String> getValidVerified(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(VERIFIED).filter(this::hasEitherTrueFalse);
    }

    private boolean hasEitherTrueFalse(String verified) {
        return Boolean.FALSE.toString().equalsIgnoreCase(verified)
               || Boolean.TRUE.toString().equalsIgnoreCase(verified);
    }

    private static String extractSectorFacet(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(SECTOR_PARAM.getNvaKey()).orElse(null);
    }

    private static String extractOrganizationFacet(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(INSTITUTION_PARAM.getNvaKey()).orElse(null);
    }

    private String getSort(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(SORT)
                   .orElse(null);
    }

    private Map<String, String> buildParametersMap(String name,
                                                   String page,
                                                   String numberOfResults,
                                                   String organization,
                                                   String verified,
                                                   String sectorFacet,
                                                   String organizationFacet,
                                                   String sort) {

        var requestQueryParameters = new ConcurrentHashMap<String, String>();

        if (nonNull(name)) {
            requestQueryParameters.put(NAME, name);
        }
        requestQueryParameters.put(PAGE, page);
        requestQueryParameters.put(NUMBER_OF_RESULTS, numberOfResults);
        if (nonNull(organization)) {
            requestQueryParameters.put(ORGANIZATION, organization);
        }
        if (nonNull(verified)) {
            requestQueryParameters.put(VERIFIED, verified);
        }
        if (nonNull(sectorFacet)) {
            requestQueryParameters.put(SECTOR_PARAM.getNvaKey(), sectorFacet);
        }
        if (nonNull(organizationFacet)) {
            requestQueryParameters.put(INSTITUTION_PARAM.getNvaKey(), organizationFacet);
        }
        if (nonNull(sort)) {
            requestQueryParameters.put(SORT, sort);
        }

        return requestQueryParameters;
    }

    private String getApiVersion(RequestInfo requestInfo) {
        return extractVersionFromRequestInfo(requestInfo, ACCEPT_HEADER_KEY_NAME);
    }

    private boolean clientIsAuthorized(RequestInfo requestInfo) {
        return requesterIsUserAdministrator(requestInfo) || clientIsCustomerAdministrator(requestInfo);
    }

}
