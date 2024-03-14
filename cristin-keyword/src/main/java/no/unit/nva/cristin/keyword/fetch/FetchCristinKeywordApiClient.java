package no.unit.nva.cristin.keyword.fetch;

import static no.unit.nva.client.HttpClientProvider.defaultHttpClient;
import static no.unit.nva.cristin.keyword.KeywordConstants.CRISTIN_KEYWORDS_PATH;
import static no.unit.nva.cristin.keyword.KeywordConstants.KEYWORD_ID_URI;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.client.FetchApiClient;
import no.unit.nva.cristin.common.client.ApiClient;
import no.unit.nva.cristin.keyword.model.nva.KeywordType;
import no.unit.nva.cristin.keyword.model.nva.KeywordTypeFromCristin;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;

public class FetchCristinKeywordApiClient extends ApiClient implements FetchApiClient<String, KeywordType> {

    public FetchCristinKeywordApiClient() {
        this(defaultHttpClient());
    }

    public FetchCristinKeywordApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public KeywordType executeFetch(String identifier) throws ApiGatewayException {
        var cristinUri = generateCristinUri(identifier);
        var response = fetchGetResult(cristinUri);
        checkHttpStatusCode(KEYWORD_ID_URI, response.statusCode(), response.body());
        var cristinKeyword = parseResponse(response);

        return convertModel(cristinKeyword);
    }

    private static URI generateCristinUri(String identifier) {
        return UriUtils.getCristinUri(identifier, CRISTIN_KEYWORDS_PATH);
    }

    private CristinTypedLabel parseResponse(HttpResponse<String> response) throws BadGatewayException {
        return getDeserializedResponse(response, CristinTypedLabel.class);
    }

    private static KeywordType convertModel(CristinTypedLabel cristinKeyword) {
        return new KeywordTypeFromCristin().apply(cristinKeyword);
    }

}
