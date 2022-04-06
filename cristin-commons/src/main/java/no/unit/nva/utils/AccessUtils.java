package no.unit.nva.utils;

import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccessUtils {

    public static final String EDIT_OWN_INSTITUTION_USERS = "EDIT_OWN_INSTITUTION_USERS";
    public static final String EDIT_OWN_INSTITUTION_PROJECTS = "EDIT_OWN_INSTITUTION_PROJECTS";
    public static final String ACCESS_TOKEN_CLAIMS_SCOPE_FIELD = "scope";
    public static final String ACCESS_TOKEN_CLAIMS_FIELD = "claims";
    public static final String AUTHORIZER_FIELD = "authorizer";
    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
        "User:{} does not have required access right:{}";
    public static final String BEARER = "Bearer";
    private static final String BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE = "https://api.nva.unit.no/scopes/backend";
    private static final Logger logger = LoggerFactory.getLogger(AccessUtils.class);

    /**
     * Validate if Requester is authorized to use IdentificationNumber to a access a user.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user is not authorized to access a user with IdentificationNumber
     */
    public static void validateIdentificationNumberAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToUseNationalIdentificationNumber(requestInfo)) {
            String nvaUsername = attempt(requestInfo::getNvaUsername).orElse(fail -> null);
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT,
                        nvaUsername, EDIT_OWN_INSTITUTION_USERS);
            throw new ForbiddenException();
        }
    }

    /**
     * Validate if Requester is authorized to create or change a project.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user has no access to create or change projects
     */
    public static void verifyRequesterCanEditProjects(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToEditProjects(requestInfo)) {
            String nvaUsername = attempt(requestInfo::getNvaUsername).orElse(fail -> null);
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT, nvaUsername, EDIT_OWN_INSTITUTION_PROJECTS);
            throw new ForbiddenException();
        }
    }

    public static boolean requesterIsUserAdministrator(RequestInfo requestInfo) {
        return requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_USERS);
    }

    private static boolean requesterHasNoAccessRightToUseNationalIdentificationNumber(RequestInfo requestInfo) {
        return !(
            requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_USERS)
            || clientIsInternalBackend(requestInfo)
        );
    }

    private static boolean clientIsInternalBackend(RequestInfo requestInfo) {
        return Optional.ofNullable(requestInfo.getRequestContext())
            .map(requestContext -> requestContext.get(AUTHORIZER_FIELD))
            .map(authorizerNode -> authorizerNode.get(ACCESS_TOKEN_CLAIMS_FIELD))
            .map(claims -> claims.get(ACCESS_TOKEN_CLAIMS_SCOPE_FIELD))
            .map(JsonNode::textValue)
            .filter(BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE::equals)
            .isPresent();
    }

    private static boolean requesterHasNoAccessRightToEditProjects(RequestInfo requestInfo) {
        return !requestInfo.userIsAuthorized(EDIT_OWN_INSTITUTION_PROJECTS);
    }
}
