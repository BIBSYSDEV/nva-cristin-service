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
import static no.unit.nva.cristin.common.ErrorMessages.validQueryParameterNamesMessage;
import static no.unit.nva.cristin.model.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.query.CristinFacetParamKey.INSTITUTION_PARAM;
import static no.unit.nva.cristin.model.query.CristinFacetParamKey.SECTOR_PARAM;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.VERIFIED;
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
                                                                     INSTITUTION_PARAM.getNvaKey());
    public static final String BOOLEAN_TRUE = "true";
    public static final String BOOLEAN_FALSE = "false";

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
    protected SearchResponse<Person> processInput(Void input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        validateQueryParameterKeys(requestInfo);

        var name = getValidName(requestInfo);
        var page = getValidPage(requestInfo);
        var numberOfResults = getValidNumberOfResults(requestInfo);
        var organization = getValidOrganization(requestInfo).orElse(null);
        var verified = getValidVerified(requestInfo).orElse(null);
        var sectorFacet = requestInfo.getQueryParameterOpt(SECTOR_PARAM.getNvaKey()).orElse(null);
        var organizationFacet = requestInfo.getQueryParameterOpt(INSTITUTION_PARAM.getNvaKey()).orElse(null);

        var requestQueryParameters = buildParametersMap(name, page, numberOfResults, organization, verified,
                                                        sectorFacet, organizationFacet);

        var apiVersion = getApiVersion(requestInfo);
        var apiClient = clientProvider.getClient(apiVersion);

        if (requesterIsUserAdministrator(requestInfo)) {
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

    private Optional<String> getValidVerified(RequestInfo requestInfo) {
        return requestInfo.getQueryParameterOpt(VERIFIED).filter(this::hasEitherTrueFalse);
    }

    private boolean hasEitherTrueFalse(String verified) {
        return BOOLEAN_FALSE.equalsIgnoreCase(verified) || BOOLEAN_TRUE.equalsIgnoreCase(verified);
    }

    private Map<String, String> buildParametersMap(String name,
                                                   String page,
                                                   String numberOfResults,
                                                   String organization,
                                                   String verified,
                                                   String sectorFacet,
                                                   String organizationFacet) {

        var requestQueryParameters = new ConcurrentHashMap<String, String>();

        requestQueryParameters.put(NAME, name);
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

        return requestQueryParameters;
    }

    private String getApiVersion(RequestInfo requestInfo) {
        return extractVersionFromRequestInfo(requestInfo, ACCEPT_HEADER_KEY_NAME);
    }

}
