package no.unit.nva.cristin.common;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import no.unit.nva.cristin.model.Constants;
import no.unit.nva.utils.AccessUtils;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import nva.commons.apigateway.exceptions.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.common.ErrorMessages.invalidPathParameterMessage;
import static no.unit.nva.cristin.model.Constants.EMPLOYMENT_ID;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.ORG_ID;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;

public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static final String PUNCTUATION = ".";
    public static final String CAN_UPDATE_ANY_INSTITUTION = "any";
    public static final String COULD_NOT_RETRIEVE_USER_CRISTIN_ORGANIZATION_IDENTIFIER =
        "Could not retrieve user's cristin organization identifier";
    public static final String USER_IS_INTERNAL_BACKEND = "User is internal backend";
    public static final String USER_TOP_LEVEL_CRISTIN_ORGANIZATION = "User has top level cristin organization {}";

    public static String forceUTF8(String value) {
        var forcedUTF8 = nonNull(value)
            ? new String(value.getBytes(), StandardCharsets.UTF_8)
            : EMPTY_STRING;
        var isUTF8 =  !forcedUTF8.isBlank()  && forcedUTF8.length() != value.length();

        return isUTF8
            ? forcedUTF8
            : value;
    }

    /**
     * Check if a string supplied is a positive integer.
     *
     * @param str String to check
     * @return a boolean with value true if string is a positive integer or else false
     */
    public static boolean isPositiveInteger(String str) {
        try {
            int value = Integer.parseInt(str);
            return value > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static <K, V> Map<K, V> nonEmptyOrDefault(Map<K, V> map) {
        return nonNull(map) ? map : Collections.emptyMap();
    }

    public static <E> List<E> nonEmptyOrDefault(List<E> list) {
        return nonNull(list) ? list : Collections.emptyList();
    }

    public static <E> Set<E> nonEmptyOrDefault(Set<E> set) {
        return nonNull(set) ? set : Collections.emptySet();
    }

    public static boolean isOrcid(String identifier) {
        return Constants.ORCID_PATTERN.matcher(identifier).matches();
    }

    /**
     * A function that can be used to filter out duplicate values based on a given key.
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Takes a Cristin unit identifier and converts it to its corresponding institution identifier.
     *
     * @return String with top level institution identifier
     */
    public static String removeUnitPartFromIdentifierIfPresent(String identifier) {
        if (nonNull(identifier) && identifier.contains(PUNCTUATION)) {
            return identifier.substring(0, identifier.indexOf(PUNCTUATION));
        }
        return identifier;
    }

    /**
     * Validates if person identifier path parameter exists and is valid format.
     */
    public static String getValidPersonId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(PERSON_ID)).orElse(fail -> EMPTY_STRING);
        if (isValidIdentifier(identifier)) {
            return identifier;
        }
        throw new BadRequestException(invalidPathParameterMessage(PERSON_ID));
    }

    /**
     * Validates if person employment identifier path parameter exists and is valid format.
     */
    public static String getValidEmploymentId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(EMPLOYMENT_ID)).orElse(fail -> EMPTY_STRING);
        if (isValidIdentifier(identifier)) {
            return identifier;
        }
        throw new BadRequestException(invalidPathParameterMessage(EMPLOYMENT_ID));
    }

    /**
     * Validates if organization identifier path parameter exists and is valid format. If supplied parameter is unit
     * identifier, the method strips it down to top level institution identifier as defined in Cristin format.
     *
     * @return top level institution identifier if input is valid identifier
     */
    public static String getValidOrgId(RequestInfo requestInfo) throws BadRequestException {
        String identifier = attempt(() -> requestInfo.getPathParameter(ORG_ID)).orElse(fail -> EMPTY_STRING);
        String cristinInstitutionId = removeUnitPartFromIdentifierIfPresent(identifier);
        if (isValidIdentifier(cristinInstitutionId)) {
            return cristinInstitutionId;
        }
        throw new BadRequestException(invalidPathParameterMessage(ORG_ID));
    }


    private static boolean isValidIdentifier(String identifier) {
        return Utils.isPositiveInteger(identifier);
    }

    /**
     * Tries to parse an input String into a json formatted ObjectNode.
     */
    public static ObjectNode readJsonFromInput(String input) throws BadRequestException {
        return attempt(() -> (ObjectNode) OBJECT_MAPPER.readTree(input))
                   .orElseThrow(fail -> new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD));
    }

    /**
     * Extracts institution string composed of digits from data stored in RequestInfo to be used as value in a header
     * sent to upstream which describes which institution the user is allowed to change employments at.
     *
     * @param requestInfo information from request used to verify allowed permissions
     * @return String with allowed institution to change. Or constant 'any' if internal backend with full access
     * @throws ForbiddenException if user it neither user administrator nor internal backend
     * @throws BadRequestException if incapable of extracting institution number
     */
    public static String extractCristinInstitutionIdentifier(RequestInfo requestInfo)
        throws BadRequestException, ForbiddenException {
        if (requestInfo.clientIsInternalBackend()) {
            logger.info(USER_IS_INTERNAL_BACKEND);
            return CAN_UPDATE_ANY_INSTITUTION;
        }
        var institution = extractInstitution(requestInfo
                                                    .getTopLevelOrgCristinId()
                                                    .orElseThrow(Utils::failedToRetrieveTopLevelOrgCristinId));
        attempt(() -> Integer.valueOf(institution)).orElseThrow(fail -> failedToRetrieveTopLevelOrgCristinId());
        logger.info(USER_TOP_LEVEL_CRISTIN_ORGANIZATION, institution);
        if (AccessUtils.requesterIsUserAdministrator(requestInfo)) {
            return institution;
        }
        throw new ForbiddenException();
    }

    private static String extractInstitution(URI organization) {
        var organizationIdentifier = UriUtils.extractLastPathElement(organization);
        return Utils.removeUnitPartFromIdentifierIfPresent(organizationIdentifier);
    }

    private static BadRequestException failedToRetrieveTopLevelOrgCristinId() {
        return new BadRequestException(COULD_NOT_RETRIEVE_USER_CRISTIN_ORGANIZATION_IDENTIFIER);
    }

    /**
     * Verifies that path parameter named 'identifier' is a numeric value and returns that value.
     *
     * @param requestInfo information from request used to extract path parameter
     * @return String containing a valid identifier
     * @throws BadRequestException if identifier is invalid
     */
    public static String getValidIdentifier(RequestInfo requestInfo) throws BadRequestException {
        attempt(() -> Integer.parseInt(requestInfo.getPathParameter(IDENTIFIER)))
            .orElseThrow(failure -> new BadRequestException(ERROR_MESSAGE_INVALID_PATH_PARAMETER_FOR_IDENTIFIER));

        return requestInfo.getPathParameter(IDENTIFIER);
    }

}
