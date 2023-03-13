package no.unit.nva.cristin.biobank;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;
import no.unit.nva.biobank.client.CristinBiobankApiClient;
import no.unit.nva.biobank.model.ParameterKeyBiobank;
import no.unit.nva.biobank.model.QueryBiobank;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.ioutils.IoUtils;

public class CristinBiobankClientStub extends CristinBiobankApiClient {

    public static final String CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE = "cristinBiobank1.json";
    public static final String CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE2 = "cristinBiobank2.json";
    private final String[] responseBody;

    public CristinBiobankClientStub() {
        this(IoUtils.stringFromResources(Path.of(CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE))
        ,IoUtils.stringFromResources(Path.of(CRISTIN_GET_BIOBANK_RESPONSE_JSON_FILE2)));
    }

    public CristinBiobankClientStub(String ...sampleResponses) {
        super(HttpClient.newBuilder().build());
        responseBody = sampleResponses;

    }

    @Override
    protected HttpResponse<String> queryBiobank(QueryBiobank query) throws ApiGatewayException {
        var biobankid = query.getValue(ParameterKeyBiobank.PATH_BIOBANK);
        var body = Arrays.stream(responseBody).filter(s -> s.contains(biobankid)).findFirst().orElse(null);
        checkHttpStatusCode(query.toURI(),200,body);
        return new HttpResponseFaker(body);
    }

}
