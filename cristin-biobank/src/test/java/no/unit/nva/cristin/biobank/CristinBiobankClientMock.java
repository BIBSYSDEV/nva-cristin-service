package no.unit.nva.cristin.biobank;

import static java.util.Objects.nonNull;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import no.unit.nva.biobank.client.CristinBiobankApiClient;
import no.unit.nva.biobank.model.ParameterKeyBiobank;
import no.unit.nva.biobank.model.QueryBiobank;
import no.unit.nva.cristin.common.ErrorMessages;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import no.unit.nva.stubs.WiremockHttpClient;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.ioutils.IoUtils;

public class CristinBiobankClientMock extends CristinBiobankApiClient {

    public static final String CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE = "cristinBiobank1.json";
    public static final String CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE2 = "cristinBiobank2.json";
    public static final String NOT_FOUND_JSON = "notFound.json";
    private final String[] responseBody;

    private final String notfoundBody;

    public CristinBiobankClientMock() {
        this(IoUtils.stringFromResources(Path.of(CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE)),
             IoUtils.stringFromResources(Path.of(CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE2)));
    }

    public CristinBiobankClientMock(String... sampleResponses) {
        super(WiremockHttpClient.create());
        responseBody = sampleResponses;
        notfoundBody = IoUtils.stringFromResources(Path.of(NOT_FOUND_JSON));
    }

    @Override
    protected HttpResponse<String> httpRequestWithStatusCheck(QueryBiobank query) throws ApiGatewayException {
        var errorMessage = String.format(ErrorMessages.ERROR_MESSAGE_IDENTIFIER_NOT_FOUND_FOR_URI, query.toURI());
        var value = query.getValue(ParameterKeyBiobank.PATH_BIOBANK);
        var body = Arrays.stream(responseBody)
                .filter(s -> s.contains(value))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(errorMessage));
        return new HttpResponseFaker(body);
    }

}
