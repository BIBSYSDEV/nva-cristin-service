package no.unit.nva.cristin.organization.common.client.v20230526;

import static java.util.Arrays.asList;
import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.CRISTIN_PER_PAGE_PARAM;
import static no.unit.nva.cristin.model.Constants.NONE;
import static no.unit.nva.cristin.model.Constants.ORGANIZATION_PATH;
import static no.unit.nva.cristin.model.Constants.PARENT_UNIT_ID;
import static no.unit.nva.cristin.model.Constants.UNITS_PATH;
import static no.unit.nva.cristin.model.JsonPropertyNames.DEPTH;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.model.Organization.ORGANIZATION_CONTEXT;
import static no.unit.nva.utils.UriUtils.createCristinQueryUri;
import static no.unit.nva.utils.UriUtils.getNvaApiId;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.cristin.organization.dto.v20230526.UnitDto;
import no.unit.nva.cristin.organization.dto.v20230526.mapper.OrganizationFromUnitMapper;
import no.unit.nva.model.Organization;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class FetchCristinOrgClient20230526 extends ApiClient
    implements FetchApiClient<Map<String, String>, Organization> {

    public static final String ALL_RESULTS = "1000"; // Max in upstream for first page
    public static final String FIRST_PAGE = "1";
    public static final String SECOND_PAGE = "2";  // Edge case where search result has up to size 2000

    public FetchCristinOrgClient20230526() {
        this(defaultHttpClient());
    }

    public FetchCristinOrgClient20230526(HttpClient client) {
        super(client);
    }

    /**
     * Fetch one organization matching given query criteria. By specifying query param depth one can choose if the
     * response should include sub organization tree downwards or just have the upwards parent trail.
     *
     * @param params Map containing verified query parameters
     */
    @Override
    public Organization executeFetch(Map<String, String> params) throws ApiGatewayException {
        var identifier = params.get(IDENTIFIER);
        var fetchUri = getCristinUri(identifier);
        var response = fetchGetResult(fetchUri);
        checkHttpStatusCode(getNvaApiId(identifier, ORGANIZATION_PATH), response.statusCode(), response.body());

        if (wantsDepth(params)) {
            var fetchSubsUri = createCristinQueryUri(translateParamsForSubUnits(identifier, FIRST_PAGE), UNITS_PATH);
            var responseWithSubs = fetchGetResult(fetchSubsUri);
            var subUnitsDto = new ArrayList<>(deserializeSubUnits(responseWithSubs));

            var multiPageProcessor = new MultiPageProcessor(responseWithSubs);
            if (multiPageProcessor.hasAdditionalPages()) {
                var fetchSubsUriPageTwo =
                    createCristinQueryUri(translateParamsForSubUnits(identifier, SECOND_PAGE), UNITS_PATH);
                var responseWithSubsPageTwo = fetchGetResult(fetchSubsUriPageTwo);

                subUnitsDto.addAll(deserializeSubUnits(responseWithSubsPageTwo));
            }

            return getMultiLevelOrganization(response, subUnitsDto);
        } else {
            var organization = getSingleLevelOrganization(response);
            organization.setContext(ORGANIZATION_CONTEXT);
            organization.setHasPart(null);

            return organization;
        }

    }

    private boolean wantsDepth(Map<String, String> params) {
        var depth = params.get(DEPTH);
        return !NONE.equals(depth);
    }

    private URI getCristinUri(String identifier) {
        return UriWrapper.fromUri(CRISTIN_API_URL)
                   .addChild(UNITS_PATH)
                   .addChild(identifier)
                   .getUri();
    }

    private Map<String, String> translateParamsForSubUnits(String identifier, String page) {
        return Map.of(PARENT_UNIT_ID, identifier, PAGE, page, CRISTIN_PER_PAGE_PARAM, ALL_RESULTS);
    }

    private List<UnitDto> deserializeSubUnits(HttpResponse<String> responseWithSubs) throws BadGatewayException {
        return asList(getDeserializedResponse(responseWithSubs, UnitDto[].class));
    }

    private Organization getMultiLevelOrganization(HttpResponse<String> response, List<UnitDto> subUnitsDto)
        throws BadGatewayException {

        var unit = getDeserializedResponse(response, UnitDto.class);
        unit.setSubUnits(subUnitsDto);
        var organization = new OrganizationFromUnitMapper().apply(unit);

        organization.setContext(ORGANIZATION_CONTEXT);

        return organization;
    }

    private Organization getSingleLevelOrganization(HttpResponse<String> response) throws BadGatewayException {
        var unit = getDeserializedResponse(response, UnitDto.class);

        return new OrganizationFromUnitMapper().apply(unit);
    }

}
