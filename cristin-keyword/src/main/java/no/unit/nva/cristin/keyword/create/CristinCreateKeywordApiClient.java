package no.unit.nva.cristin.keyword.create;

import static no.unit.nva.cristin.keyword.KeywordConstants.CRISTIN_KEYWORDS_PATH;
import static no.unit.nva.cristin.keyword.KeywordConstants.KEYWORD_PATH;
import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.utils.UriUtils.getNvaApiUri;
import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import no.unit.nva.cristin.common.client.PostApiClient;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.model.adapter.CristinTypedLabelToNvaFormat;
import no.unit.nva.model.adapter.TypedLabelToCristinFormat;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadGatewayException;
import nva.commons.core.paths.UriWrapper;

public class CristinCreateKeywordApiClient extends PostApiClient implements CreateKeywordApiClient {

    public CristinCreateKeywordApiClient(HttpClient client) {
        super(client);
    }

    @Override
    public TypedLabel create(TypedLabel input) throws ApiGatewayException {
        var payload = generatePayloadFromRequest(input);
        var uri = getPostUri();
        var response = post(uri, payload);
        checkPostHttpStatusCode(getNvaApiUri(KEYWORD_PATH), response.statusCode(), response.body());

        return keywordFromResponse(response);
    }

    private String generatePayloadFromRequest(TypedLabel input) {
        return attempt(() -> OBJECT_MAPPER.writeValueAsString(new TypedLabelToCristinFormat().apply(input)))
                   .orElseThrow();
    }

    private URI getPostUri() {
        return UriWrapper.fromUri(CRISTIN_API_URL).addChild(CRISTIN_KEYWORDS_PATH).getUri();
    }

    private TypedLabel keywordFromResponse(HttpResponse<String> response) throws BadGatewayException {
        var responseCristinTypedLabel = getDeserializedResponse(response, CristinTypedLabel.class);
        return new CristinTypedLabelToNvaFormat().apply(responseCristinTypedLabel);
    }

}
