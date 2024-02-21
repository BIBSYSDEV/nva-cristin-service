package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.COLLABORATION;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.COUNTRIES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NVI;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PLACE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.validation.PatchValidator.validateDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Optional;
import no.unit.nva.cristin.common.Utils;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.employment.create.CreatePersonEmploymentValidator;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.cristin.person.model.nva.PersonSummary;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.utils.UriUtils;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.JacocoGenerated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PersonPatchValidator {

    private static final Logger logger = LoggerFactory.getLogger(PersonPatchValidator.class);

    public static final String ORCID_IS_NOT_VALID = "ORCID is not valid";
    public static final String FIELD_CAN_NOT_BE_ERASED = "Field %s can not be erased";
    public static final String RESERVED_MUST_BE_BOOLEAN =
        "Reserved field can only be set to boolean value";
    public static final String COULD_NOT_PARSE_EMPLOYMENT_FIELD = "Could not parse employment field because of "
                                                                  + "invalid data";
    private static final String EXCEPTION_WHEN_VALIDATING_EMPLOYMENTS = "Exception when validating employments: {}";
    private static final String EXCEPTION_WHEN_VALIDATING_KEYWORDS = "Exception when validating keywords: {}";
    public static final String COULD_NOT_PARSE_KEYWORD_FIELD = "Could not parse keyword field because of "
                                                                  + "invalid data";
    public static final String EXCEPTION_WHEN_VALIDATING_PERSON_NVI = "Exception when validating personNvi: {}";
    public static final String COULD_NOT_PARSE_NVI_FIELD = "Could not parse personNvi field because of invalid data";
    public static final String MUST_HAVE_A_VALID_ORGANIZATION_IDENTIFIER =
        "Person NVI data must have a valid Organization identifier";
    public static final String MUST_HAVE_A_VALID_PERSON_IDENTIFIER =
        "Person NVI data must have a valid Person identifier";
    private static final String EXCEPTION_WHEN_VALIDATING_COUNTRIES = "Exception when validating countries: {}";
    public static final String COULD_NOT_PARSE_COUNTRIES_FIELD = "Could not parse countries field because of "
                                                               + "invalid data";

    @JacocoGenerated
    private PersonPatchValidator() {
        // NO-OP
    }

    /**
     * Validate according to rules of upstream json schema.
     */
    public static void validate(ObjectNode input) throws BadRequestException {
        validateOrcidIfPresent(input);
        validateFirstNameIfPresent(input);
        validateLastNameIfPresent(input);
        validateReservedIfPresent(input);
        validateEmploymentsIfPresent(input);
        validateKeywordsIfPresent(input);
        validateDescription(input, BACKGROUND);
        validateNviIfPresent(input);
        validateDescription(input, PLACE);
        validateDescription(input, COLLABORATION);
        validateCountriesIfPresent(input);
    }

    /**
     * Validate user modifiable fields according to rules of upstream json schema.
     */
    public static void validateUserModifiableFields(ObjectNode input) throws BadRequestException {
        validateOrcidIfPresent(input);
        validateKeywordsIfPresent(input);
        validateDescription(input, BACKGROUND);
        validateDescription(input, PLACE);
        validateDescription(input, COLLABORATION);
        validateCountriesIfPresent(input);
    }

    /**
     * Validate an orcid if present in input node.
     */
    public static void validateOrcidIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(ORCID) && !input.get(ORCID).isNull() && !Utils.isOrcid(input.get(ORCID).asText())) {
            throw new BadRequestException(ORCID_IS_NOT_VALID);
        }
    }

    private static void validateFirstNameIfPresent(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, FIRST_NAME);
    }

    private static void validateLastNameIfPresent(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, LAST_NAME);
    }

    private static void validateNotNullIfPresent(ObjectNode input, String fieldName) throws BadRequestException {
        if (input.has(fieldName) && input.get(fieldName).isNull()) {
            throw new BadRequestException(String.format(FIELD_CAN_NOT_BE_ERASED, fieldName));
        }
    }

    private static void validateReservedIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(RESERVED) && !input.get(RESERVED).isBoolean()) {
            throw new BadRequestException(RESERVED_MUST_BE_BOOLEAN);
        }
    }

    private static void validateEmploymentsIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(EMPLOYMENTS) && !input.get(EMPLOYMENTS).isNull()) {
            var employments = parseEmployments(input);
            for (Employment employment : employments) {
                CreatePersonEmploymentValidator.validate(employment);
            }
        }
    }

    private static Employment[] parseEmployments(ObjectNode input) throws BadRequestException {
        try {
            return OBJECT_MAPPER.readValue(input.get(EMPLOYMENTS).toString(), Employment[].class);
        } catch (JsonProcessingException e) {
            logger.warn(EXCEPTION_WHEN_VALIDATING_EMPLOYMENTS, e.getMessage());
            throw new BadRequestException(COULD_NOT_PARSE_EMPLOYMENT_FIELD);
        }
    }

    private static void validateKeywordsIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(KEYWORDS) && !input.get(KEYWORDS).isNull()) {
            parseKeywords(input);
        }
    }

    private static void parseKeywords(ObjectNode input) throws BadRequestException {
        try {
            OBJECT_MAPPER.readValue(input.get(KEYWORDS).toString(), TypedValue[].class);
        } catch (JsonProcessingException e) {
            logger.warn(EXCEPTION_WHEN_VALIDATING_KEYWORDS, e.getMessage());
            throw new BadRequestException(COULD_NOT_PARSE_KEYWORD_FIELD);
        }
    }

    private static void validateNviIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(NVI) && !input.get(NVI).isNull()) {
            try {
                var personNvi = OBJECT_MAPPER.readValue(input.get(NVI).toString(), PersonNvi.class);

                Optional.ofNullable(personNvi)
                    .map(PersonNvi::verifiedAt)
                    .map(CristinUnit::extractUnitIdentifier)
                    .filter(Optional::isPresent)
                    .orElseThrow(() -> invalidIdentifier(MUST_HAVE_A_VALID_ORGANIZATION_IDENTIFIER));

                Optional.of(personNvi)
                    .map(PersonNvi::verifiedBy)
                    .map(PersonSummary::id)
                    .map(UriUtils::extractLastPathElement)
                    .filter(Utils::isPositiveInteger)
                    .orElseThrow(() -> invalidIdentifier(MUST_HAVE_A_VALID_PERSON_IDENTIFIER));
            } catch (JsonProcessingException e) {
                logger.warn(EXCEPTION_WHEN_VALIDATING_PERSON_NVI, e.getMessage());
                throw new BadRequestException(COULD_NOT_PARSE_NVI_FIELD);
            }
        }
    }

    private static BadRequestException invalidIdentifier(String msg) {
        return new BadRequestException(msg);
    }

    private static void validateCountriesIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(COUNTRIES) && !input.get(COUNTRIES).isNull()) {
            parseCountries(input);
        }
    }

    private static void parseCountries(ObjectNode input) throws BadRequestException {
        try {
            OBJECT_MAPPER.readValue(input.get(COUNTRIES).toString(), TypedLabel[].class);
        } catch (JsonProcessingException e) {
            logger.warn(EXCEPTION_WHEN_VALIDATING_COUNTRIES, e.getMessage());
            throw new BadRequestException(COULD_NOT_PARSE_COUNTRIES_FIELD);
        }
    }
}
