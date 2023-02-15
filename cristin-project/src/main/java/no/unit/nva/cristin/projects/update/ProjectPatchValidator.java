package no.unit.nva.cristin.projects.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.util.Map;
import no.unit.nva.Validator;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.model.Organization;
import no.unit.nva.utils.PatchValidator;
import nva.commons.apigateway.exceptions.BadRequestException;

import java.util.List;

import static java.lang.String.format;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_CATEGORIES;
import static no.unit.nva.cristin.model.JsonPropertyNames.RELATED_PROJECTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.EQUIPMENT;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.KEYWORDS;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.METHOD;
import static no.unit.nva.cristin.projects.model.nva.Funding.CODE;
import static no.unit.nva.cristin.projects.model.nva.Funding.SOURCE;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static nva.commons.core.attempt.Try.attempt;


@SuppressWarnings("PMD.GodClass")
public class ProjectPatchValidator extends PatchValidator implements Validator<ObjectNode> {

    public static final String TITLE_MUST_HAVE_A_LANGUAGE = "Title must have a language associated";
    public static final String FUNDING_MISSING_REQUIRED_FIELDS = "Funding missing required fields";
    public static final String MUST_BE_A_LIST = "Field %s must be a list";
    public static final String KEYWORDS_MISSING_REQUIRED_FIELD_TYPE = "Keywords missing required field 'type'";
    public static final String PROJECT_CATEGORIES_MISSING_REQUIRED_FIELD_TYPE = "ProjectCategories missing required "
                                                                                + "field 'type'";
    public static final String MUST_BE_A_LIST_OF_IDENTIFIERS = "RelatedProjects must be a list of identifiers, "
                                                               + "numeric or URI";
    public static final String NOT_A_VALID_KEY_VALUE_FIELD = "%s not a valid key value field";

    /**
     * Validate changes to Project, both nullable fields and values.
     *
     * @param input ObjectNode containing fields with input data to be changed.
     * @throws BadRequestException thrown when input has illegal or invalid values.
     */
    @Override
    public void validate(ObjectNode input) throws BadRequestException {
        validateTitleAndLanguage(input);
        validateContributors(input);
        validateCoordinatingInstitution(input);
        validateInstantIfPresent(input, END_DATE);
        validateInstantIfPresent(input, START_DATE);
        validateLanguage(input);
        validateFundingsIfPresent(input);
        validateKeywordsIfPresent(input);
        validateProjectCategoriesIfPresent(input);
        validateRelatedProjects(input);
        validateDescription(input, ACADEMIC_SUMMARY);
        validateDescription(input, POPULAR_SCIENTIFIC_SUMMARY);
        validateDescription(input, METHOD);
        validateDescription(input, EQUIPMENT);
        validateResearchResponsibleOrganizationsIfPresent(input);
    }

    private static void validateTitleAndLanguage(ObjectNode input) throws BadRequestException {
        // Language must have value if present, title can be 'nulled'
        validateNotNullIfPresent(input, LANGUAGE);
        if (propertyHasValue(input, TITLE) && !propertyHasValue(input, LANGUAGE)) {
            throw new BadRequestException(TITLE_MUST_HAVE_A_LANGUAGE);
        }
    }

    private static void validateCoordinatingInstitution(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, COORDINATING_INSTITUTION);
        if (propertyHasValue(input, COORDINATING_INSTITUTION)) {
            attempt(() -> OBJECT_MAPPER.readValue(input.get(COORDINATING_INSTITUTION).toString(), Organization.class))
                    .orElseThrow(fail ->
                        new BadRequestException(format(ILLEGAL_VALUE_FOR_PROPERTY, COORDINATING_INSTITUTION)));
        }

    }

    private static void validateContributors(ObjectNode input) throws BadRequestException {
        validateNotNullIfPresent(input, CONTRIBUTORS);
        if (propertyHasValue(input, CONTRIBUTORS)) {
            TypeReference<List<NvaContributor>> typeRef = new TypeReference<>() {
            };
            validateJsonReadable(typeRef, input.get(CONTRIBUTORS).toString(), CONTRIBUTORS);
        }
    }

    private static void validateJsonReadable(TypeReference<?> typeRef, String content, String propertyName)
            throws BadRequestException {
        attempt(() -> OBJECT_MAPPER.readValue(content, typeRef))
                .orElseThrow(fail ->
                        new BadRequestException(format(ILLEGAL_VALUE_FOR_PROPERTY, propertyName)));
    }

    private static void validateFundingsIfPresent(ObjectNode input) throws BadRequestException {
        if (!input.has(FUNDING) || input.get(FUNDING).isNull()) {
            return;
        }
        if (input.get(FUNDING).isArray()) {
            var fundingArray = (ArrayNode) input.get(FUNDING);
            for (JsonNode funding : fundingArray) {
                validateFunding(funding);
            }
        } else {
            throw new BadRequestException(format(MUST_BE_A_LIST, FUNDING));
        }
    }

    private static void validateFunding(JsonNode funding) throws BadRequestException {
        if (!funding.has(SOURCE) || funding.get(SOURCE).isNull()
            || !funding.get(SOURCE).has(CODE) || funding.get(SOURCE).get(CODE).isNull()) {
            throw new BadRequestException(FUNDING_MISSING_REQUIRED_FIELDS);
        }
    }

    private void validateKeywordsIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(KEYWORDS)) {
            var keywords = input.get(KEYWORDS);
            validateIsArray(keywords, KEYWORDS);
            validateKeywords(keywords);
        }
    }

    private void validateKeywords(JsonNode keywords) throws BadRequestException {
        for (JsonNode keyword : keywords) {
            if (typedValueFieldNotValid(keyword)) {
                throw new BadRequestException(KEYWORDS_MISSING_REQUIRED_FIELD_TYPE);
            }
        }
    }

    private void validateProjectCategoriesIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(PROJECT_CATEGORIES)) {
            var projectCategories = input.get(PROJECT_CATEGORIES);
            validateIsArray(projectCategories, PROJECT_CATEGORIES);
            validateEachProjectCategory(projectCategories);
        }
    }

    private void validateEachProjectCategory(JsonNode projectCategories) throws BadRequestException {
        for (JsonNode node : projectCategories) {
            if (typedValueFieldNotValid(node)) {
                throw new BadRequestException(PROJECT_CATEGORIES_MISSING_REQUIRED_FIELD_TYPE);
            }
        }
    }

    private boolean typedValueFieldNotValid(JsonNode node) {
        return !node.has(TYPE) || node.get(TYPE).isNull() || node.get(TYPE).asText().isBlank();
    }

    private void validateIsArray(JsonNode node, String fieldName) throws BadRequestException {
        if (!node.isArray()) {
            throw new BadRequestException(format(MUST_BE_A_LIST, fieldName));
        }
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void validateRelatedProjects(ObjectNode input) throws BadRequestException {
        if (input.has(RELATED_PROJECTS) && !input.get(RELATED_PROJECTS).isNull()) {
            var relatedProjects = input.get(RELATED_PROJECTS);
            validateIsArray(relatedProjects, RELATED_PROJECTS);
            for (JsonNode project : relatedProjects) {
                attempt(() -> extractLastPathElement(URI.create(project.asText())))
                    .orElseThrow(failure -> new BadRequestException(MUST_BE_A_LIST_OF_IDENTIFIERS));
            }
        }
    }

    private void validateDescription(ObjectNode input, String fieldName) throws BadRequestException {
        if (input.has(fieldName)) {
            var description = input.get(fieldName);
            attempt(() -> OBJECT_MAPPER.convertValue(description, new TypeReference<Map<String, String>>(){}))
                .orElseThrow(failure -> new BadRequestException(format(NOT_A_VALID_KEY_VALUE_FIELD, fieldName)));
        }
    }

    private void validateResearchResponsibleOrganizationsIfPresent(ObjectNode input) throws BadRequestException {
        if (input.has(NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH)
            && !input.get(NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH).isNull()) {
            var organizations = input.get(NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH);
            validateIsArray(organizations, NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH);
            try {
                parseOrganizations(organizations);
            } catch (Exception e) {
                throw invalidOrganizationsException();
            }
        }
    }

    private BadRequestException invalidOrganizationsException() {
        return new BadRequestException(format(ILLEGAL_VALUE_FOR_PROPERTY, NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH));
    }

    private void parseOrganizations(JsonNode organizations) {
        organizations.forEach(node -> extractLastPathElement(URI.create(node.get(ID).asText())));
    }
}
