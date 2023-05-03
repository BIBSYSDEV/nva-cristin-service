package no.unit.nva.cristin.person.picture.update;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import com.amazonaws.services.lambda.runtime.Context;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.cristin.person.model.nva.Binary;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePictureHandler extends ApiGatewayHandler<Binary, Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePictureHandler.class);
    public static final String IS_ACTING_AS_THEMSELVES = " and is acting as themselves";

    private final transient UpdatePictureApiClient apiClient;

    @SuppressWarnings("unused")
    public UpdatePictureHandler() {
        this(new UpdatePictureApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public UpdatePictureHandler(UpdatePictureApiClient apiClient, Environment environment) {
        super(Binary.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Void processInput(Binary input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        new UpdatePictureAccessCheck().verifyAccess(requestInfo);

        logger.info(LOG_IDENTIFIERS + IS_ACTING_AS_THEMSELVES, extractCristinIdentifier(requestInfo),
                    extractOrgIdentifier(requestInfo));

        var decoded = new UpdatePictureContentVerifier(input).getDecoded();

        return apiClient.uploadPicture(getValidPersonId(requestInfo), decoded);
    }

    @Override
    protected Integer getSuccessStatusCode(Binary input, Void output) {
        return HTTP_NO_CONTENT;
    }

}
