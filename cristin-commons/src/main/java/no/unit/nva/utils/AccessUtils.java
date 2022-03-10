package no.unit.nva.utils;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.core.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.Base64;

import static no.unit.nva.cristin.common.client.ApiClient.AUTHORIZATION;

public class AccessUtils {

    public static final String EDIT_OWN_INSTITUTION_USERS = "EDIT_OWN_INSTITUTION_USERS";
    public static final String EDIT_OWN_INSTITUTION_PROJECTS = "EDIT_OWN_INSTITUTION_PROJECTS";

    public static final String ACCESS_TOKEN_CLAIMS_SCOPE_FIELD = "scope";
    public static final String ACCESS_TOKEN_CLAIMS_FIELD = "claims";
    public static final String AUTHORIZER_FIELD = "authorizer";
    public static final String USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT =
        "User:{} does not have required access right:{}";
    private static final Logger logger = LoggerFactory.getLogger(AccessUtils.class);
    private static final String BACKEND_SCOPE_AS_DEFINED_IN_IDENTITY_SERVICE = "https://api.nva.unit.no/scopes/backend";
            "User:{} does not have required access right:{}";
    public static final String BEARER = "Bearer";
    public static final String DOT_SEPARATOR = "\\.";
    public static final int PAYLOAD_SEGMENT = 1;
    public static final String CUSTOM_ACCESS_RIGHTS = "custom:accessRights";
    private static final Logger logger = LoggerFactory.getLogger(AccessUtils.class);

    /**
     * Validate if Requester is authorized to use IdentificationNumber to a access a user.
     *
     * @param requestInfo information from request
     * @throws ForbiddenException thrown when user is not authorized to access a user with IdentificationNumber
     */
    public static void validateIdentificationNumberAccess(RequestInfo requestInfo) throws ForbiddenException {
        if (requesterHasNoAccessRightToUseNationalIdentificationNumber(requestInfo)) {
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT,
                        requestInfo.getFeideId(), EDIT_OWN_INSTITUTION_USERS);
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
            logger.warn(USER_DOES_NOT_HAVE_REQUIRED_ACCESS_RIGHT,
                        requestInfo.getFeideId(), EDIT_OWN_INSTITUTION_PROJECTS);
            throw new ForbiddenException();
        }
    }

    public static boolean requesterIsUserAdministrator(RequestInfo requestInfo) {
        return requestInfo.getAccessRights().contains(EDIT_OWN_INSTITUTION_USERS)
                || requesterHasAccessRightInBearerToken(requestInfo);
    }

    private static boolean requesterHasNoAccessRightToUseNationalIdentificationNumber(RequestInfo requestInfo) {
        return !(
            requestInfo.getAccessRights().contains(EDIT_OWN_INSTITUTION_USERS)
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
        return !requestInfo.getAccessRights().contains(EDIT_OWN_INSTITUTION_PROJECTS);
    }

    private static boolean requesterHasAccessRightInBearerToken(RequestInfo requestInfo) {
        try {
            final var authorizationHeader = requestInfo.getHeaders().get(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
                var token = authorizationHeader.substring(BEARER.length() + 1, authorizationHeader.length());
                var payload = new String(Base64.getUrlDecoder().decode(token.split(DOT_SEPARATOR)[PAYLOAD_SEGMENT]));
                var accessRights = JsonUtils.dtoObjectMapper.readTree(new StringReader(payload))
                        .get(CUSTOM_ACCESS_RIGHTS).asText();
                return accessRights.contains(EDIT_OWN_INSTITUTION_USERS);
            }
        } catch (Exception ignored) {
            logger.debug("No valid access information in request authorization header");
        }
        return false;
    }

}
