package no.unit.nva.cristin.person.picture;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import static no.unit.nva.utils.LogUtils.LOG_IDENTIFIERS;
import static no.unit.nva.utils.LogUtils.extractCristinIdentifier;
import static no.unit.nva.utils.LogUtils.extractOrgIdentifier;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.cristin.common.client.CristinAuthenticator;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.ApiGatewayHandler;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import nva.commons.core.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class UpdatePictureHandler extends ApiGatewayHandler<InputStream, Void> {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePictureHandler.class);
    public static final String STARTING_UPDATE_MESSAGE =
        "User is acting as themselves. Uploading profile picture to upstream";
    public static final String USER_TRYING_TO_UPDATE_ANOTHER_PERSONS_PICTURE =
        "User logged in with id {} but trying to update profile picture under id {}";
    public static final String COULD_NOT_RETRIEVE_CLIENT_PERSON_ID = "Could not retrieve client person id from request";

    private final transient PictureApiClient apiClient;

    public UpdatePictureHandler() {
        this(new PictureApiClient(CristinAuthenticator.getHttpClient()), new Environment());
    }

    public UpdatePictureHandler(PictureApiClient apiClient, Environment environment) {
        super(InputStream.class, environment);
        this.apiClient = apiClient;
    }

    @Override
    protected Void processInput(InputStream input, RequestInfo requestInfo, Context context)
        throws ApiGatewayException {

        var personIdFromPath = getValidPersonId(requestInfo);
        var personIdFromCognito =
            getValidPersonIdFromCognito(requestInfo).orElseThrow(this::couldNotGetPersonIdFromCognito);
        validateUserIsThemselvesOrElseDenyAccess(personIdFromPath, personIdFromCognito);

        logger.info(LOG_IDENTIFIERS, extractCristinIdentifier(requestInfo), extractOrgIdentifier(requestInfo));
        logger.info(STARTING_UPDATE_MESSAGE);

        apiClient.uploadPicture(personIdFromPath, input);

        return null;
    }

    @Override
    protected Integer getSuccessStatusCode(InputStream input, Void output) {
        return HTTP_NO_CONTENT;
    }

    private Optional<String> getValidPersonIdFromCognito(RequestInfo requestInfo) throws UnauthorizedException {
        return Optional.ofNullable(requestInfo.getPersonCristinId()).map(UriUtils::extractLastPathElement);
    }

    private UnauthorizedException couldNotGetPersonIdFromCognito() {
        return new UnauthorizedException(COULD_NOT_RETRIEVE_CLIENT_PERSON_ID);
    }

    private void validateUserIsThemselvesOrElseDenyAccess(String personIdFromPath, String personIdFromCognito)
        throws ForbiddenException {

        if (!Objects.equals(personIdFromPath, personIdFromCognito)) {
            logger.info(USER_TRYING_TO_UPDATE_ANOTHER_PERSONS_PICTURE, personIdFromCognito, personIdFromPath);
            throw new ForbiddenException();
        }
    }

}
