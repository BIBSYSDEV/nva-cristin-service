package no.unit.nva.cristin.person.picture;

import static no.unit.nva.cristin.common.Utils.getValidPersonId;
import java.util.Objects;
import java.util.Optional;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.apigateway.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePictureAccessCheck {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePictureAccessCheck.class);

    public static final String USER_TRYING_TO_UPDATE_ANOTHER_PERSONS_PICTURE =
        "User logged in with id {} but trying to update profile picture under id {}";
    public static final String COULD_NOT_RETRIEVE_CLIENT_PERSON_ID = "Could not retrieve client person id from request";

    /**
     * Verifies that the user is logged in and only tries to update their own profile picture.
     */
    public void verifyAccess(RequestInfo requestInfo) throws ApiGatewayException {
        var personIdFromPath = getValidPersonId(requestInfo);
        var personIdFromCognito =
            getValidPersonIdFromCognito(requestInfo).orElseThrow(this::couldNotGetPersonIdFromCognito);
        validateUserIsThemselvesOrElseDenyAccess(personIdFromPath, personIdFromCognito);
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
