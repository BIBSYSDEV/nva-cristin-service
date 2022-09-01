package no.unit.nva.cristin.common;

import no.unit.nva.cristin.model.Constants;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.common.ErrorMessages.invalidPathParameterMessage;
import static no.unit.nva.cristin.model.Constants.EMPLOYMENT_ID;
import static no.unit.nva.cristin.model.Constants.ORG_ID;
import static no.unit.nva.cristin.model.Constants.PERSON_ID;
import static nva.commons.apigateway.RestRequestHandler.EMPTY_STRING;
import static nva.commons.core.attempt.Try.attempt;

public class Utils {

    public static final String PUNCTUATION = ".";

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

}
