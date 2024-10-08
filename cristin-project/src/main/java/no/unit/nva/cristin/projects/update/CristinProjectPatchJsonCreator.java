package no.unit.nva.cristin.projects.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import no.unit.nva.cristin.model.CristinOrganization;
import java.util.HashMap;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.projects.model.cristin.adapter.NvaContributorToCristinPerson;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.language.LanguageDescription;
import no.unit.nva.model.Organization;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingUnitIfPresent;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTACT_INFO;
import static no.unit.nva.cristin.model.JsonPropertyNames.ALTERNATIVE_TITLES;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_CONTACT_INFO;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.EMAIL;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH;
import static no.unit.nva.cristin.model.JsonPropertyNames.ORGANIZATION;
import static no.unit.nva.cristin.model.JsonPropertyNames.PHONE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_CATEGORIES;
import static no.unit.nva.cristin.model.JsonPropertyNames.RELATED_PROJECTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.projects.model.cristin.CristinContactInfo.CRISTIN_CONTACT_PERSON;
import static no.unit.nva.cristin.projects.model.cristin.CristinFundingSource.extractFundingSourceCode;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_EXTERNAL_URL;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_MAIN_LANGUAGE;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_PROJECT_CATEGORIES;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_RELATED_PROJECTS;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.EQUIPMENT;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.KEYWORDS;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.METHOD;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.PROJECT_FUNDING_SOURCES;
import static no.unit.nva.cristin.projects.model.nva.ContactInfo.CONTACT_PERSON;
import static no.unit.nva.cristin.projects.model.nva.Funding.SOURCE;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.WEB_PAGE;
import static no.unit.nva.cristin.projects.update.ProjectPatchValidator.convertToMap;
import static no.unit.nva.language.LanguageConstants.UNDEFINED_LANGUAGE;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static no.unit.nva.utils.CustomInstantSerializer.addMillisToInstantString;
import static no.unit.nva.utils.UriUtils.extractLastPathElement;
import static nva.commons.core.attempt.Try.attempt;


public class CristinProjectPatchJsonCreator {

    public static final String CRISTIN_COORDINATING_INSTITUTION = "coordinating_institution";
    public static final String PARTICIPANTS = "participants";

    private final transient ObjectNode input;
    private final transient ObjectNode output;

    public CristinProjectPatchJsonCreator(ObjectNode objectNode) {
        input = objectNode;
        output = OBJECT_MAPPER.createObjectNode();
    }

    /**
     * Class for creating json matching Cristin schema for Project update.
     * Aktuelle kandidater:
     * title coordinating_institution institutions_responsible_for_research start_date end_date participants
     */
    public CristinProjectPatchJsonCreator create() {
        addTitleAndLanguageIfPresent();
        addCoordinatingInstitutionIfPresent();
        addContributorsIfPresent();
        addStartDateIfPresent();
        addEndDateIfPresent();
        addFundingIfPresent();
        addKeywordsIfPresent();
        addProjectCategoriesIfPresent();
        addRelatedProjectsIfPresent();
        addAcademicSummaryIfpresent();
        addPopularScientificSummaryIfPresent();
        addMethodIfPresent();
        addEquipmentIfPresent();
        addContactInfoIfPresent();
        addResponsibleInstitutionsIfPresent();
        addWebPageIfPresent();

        return this;
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private void addTitleAndLanguageIfPresent() {
        var cristinTitles = new HashMap<String, String>();

        if (input.has(LANGUAGE)) {
            final var language = getLanguageByUri(URI.create(input.get(LANGUAGE).asText()));
            if (nonNull(language) && isSupportedLanguage(language)) {
                output.put(CRISTIN_MAIN_LANGUAGE, language.getIso6391Code());
                final var title = input.get(TITLE);
                if (nonNull(title)) {
                    cristinTitles.put(language.getIso6391Code(), title.asText());
                }
            }
        }

        if (input.has(ALTERNATIVE_TITLES) && !input.get(ALTERNATIVE_TITLES).isNull()) {
            var alternativeTitles = input.get(ALTERNATIVE_TITLES);
            for (JsonNode title : alternativeTitles) {
                var map = convertToMap(title);
                cristinTitles.putAll(map);
            }
        }

        if (!cristinTitles.isEmpty()) {
            output.putPOJO(TITLE, cristinTitles);
        }
    }

    private boolean isSupportedLanguage(LanguageDescription language) {
        return !UNDEFINED_LANGUAGE.equals(language);
    }

    private void addCoordinatingInstitutionIfPresent() {
        if (input.has(COORDINATING_INSTITUTION)) {
            var coordinatingInstitution =
                    attempt(() -> OBJECT_MAPPER.readValue(input.get(COORDINATING_INSTITUTION).toString(),
                            Organization.class))
                            .orElseThrow();

            var cristinOrganization = extractCristinOrganization(coordinatingInstitution);
            output.set(CRISTIN_COORDINATING_INSTITUTION, OBJECT_MAPPER.valueToTree(cristinOrganization));
        }
    }

    private void addContributorsIfPresent() {
        if (input.has(CONTRIBUTORS)) {
            var contributors =
                    attempt(() -> {
                        var typeRef = new TypeReference<List<NvaContributor>>() { };
                        return OBJECT_MAPPER.readValue(input.get(CONTRIBUTORS).toString(), typeRef);
                    }).orElseThrow();
            output.set(PARTICIPANTS, OBJECT_MAPPER.valueToTree(extractContributors(contributors)));
        }
    }

    private void addStartDateIfPresent() {
        if (input.has(START_DATE)) {
            output.put(CRISTIN_START_DATE, addMillisToInstantString(input.get(START_DATE).asText()));
        }
    }

    private void addEndDateIfPresent() {
        if (input.has(END_DATE)) {
            output.put(CRISTIN_END_DATE, addMillisToInstantString(input.get(END_DATE).asText()));
        }
    }

    public ObjectNode getOutput() {
        return output;
    }

    private static List<CristinPerson> extractContributors(List<NvaContributor> contributors) {
        return contributors.stream()
                   .map(new NvaContributorToCristinPerson())
                   .collect(Collectors.toList());
    }

    private void addFundingIfPresent() {
        if (input.has(FUNDING)) {
            if (input.get(FUNDING).isNull()) {
                output.putNull(PROJECT_FUNDING_SOURCES);
                return;
            }

            var cristinFundingSources = new ArrayList<CristinFundingSource>();
            var fundingSources = (ArrayNode) input.get(FUNDING);
            fundingSources.forEach(node -> cristinFundingSources.add(oneFundingToCristinFunding(node)));

            output.set(PROJECT_FUNDING_SOURCES, OBJECT_MAPPER.valueToTree(cristinFundingSources));
        }
    }

    private CristinFundingSource oneFundingToCristinFunding(JsonNode fundingSource) {
        var cristinFundingSource = new CristinFundingSource();
        var sourceCode = extractFundingSourceCode(URI.create(fundingSource.get(SOURCE).textValue()));
        cristinFundingSource.setFundingSourceCode(sourceCode);
        if (fundingSource.has(Funding.IDENTIFIER)) {
            var projectCode = fundingSource.get(Funding.IDENTIFIER).asText();
            cristinFundingSource.setProjectCode(projectCode);
        }

        return cristinFundingSource;
    }

    private void addKeywordsIfPresent() {
        if (input.has(KEYWORDS)) {
            addTypedLabelsUsingFieldNames(KEYWORDS, KEYWORDS);
        }
    }

    private void addProjectCategoriesIfPresent() {
        if (input.has(PROJECT_CATEGORIES)) {
            addTypedLabelsUsingFieldNames(PROJECT_CATEGORIES, CRISTIN_PROJECT_CATEGORIES);
        }
    }

    private void addTypedLabelsUsingFieldNames(String fieldName, String outputFieldName) {
        if (input.get(fieldName).isNull()) {
            output.putNull(fieldName);
        }

        var cristinLabels = new ArrayList<CristinTypedLabel>();
        var labels = (ArrayNode) input.get(fieldName);
        labels.forEach(node -> cristinLabels.add(new CristinTypedLabel(node.get(TYPE).asText(), null)));

        output.set(outputFieldName, OBJECT_MAPPER.valueToTree(cristinLabels));
    }

    private void addRelatedProjectsIfPresent() {
        if (input.has(RELATED_PROJECTS)) {
            var relatedProjects = input.get(RELATED_PROJECTS);
            if (relatedProjects.isNull()) {
                output.putNull(CRISTIN_RELATED_PROJECTS);
                return;
            }
            var identifiers = new ArrayList<String>();
            for (JsonNode project : relatedProjects) {
                identifiers.add(extractLastPathElement(URI.create(project.asText())));
            }
            output.putPOJO(CRISTIN_RELATED_PROJECTS, identifiers);
        }
    }

    private void addAcademicSummaryIfpresent() {
        if (input.has(ACADEMIC_SUMMARY)) {
            output.set(CRISTIN_ACADEMIC_SUMMARY, input.get(ACADEMIC_SUMMARY));
        }
    }

    private void addPopularScientificSummaryIfPresent() {
        if (input.has(POPULAR_SCIENTIFIC_SUMMARY)) {
            output.set(CRISTIN_POPULAR_SCIENTIFIC_SUMMARY, input.get(POPULAR_SCIENTIFIC_SUMMARY));
        }
    }

    private void addMethodIfPresent() {
        if (input.has(METHOD)) {
            output.set(METHOD, input.get(METHOD));
        }
    }

    private void addEquipmentIfPresent() {
        if (input.has(EQUIPMENT)) {
            output.set(EQUIPMENT, input.get(EQUIPMENT));
        }
    }

    private void addContactInfoIfPresent() {
        if (input.has(CONTACT_INFO) && !input.get(CONTACT_INFO).isNull()) {
            var contactInfoInput = input.get(CONTACT_INFO);
            var contactInfoOutput = OBJECT_MAPPER.createObjectNode();

            addNullableStringField(contactInfoInput, contactInfoOutput, CONTACT_PERSON, CRISTIN_CONTACT_PERSON);
            addNullableStringField(contactInfoInput, contactInfoOutput, ORGANIZATION, INSTITUTION);
            addNullableStringField(contactInfoInput, contactInfoOutput, EMAIL, EMAIL);
            addNullableStringField(contactInfoInput, contactInfoOutput, PHONE, PHONE);

            output.set(CRISTIN_CONTACT_INFO, contactInfoOutput);
        }
    }

    private void addNullableStringField(JsonNode input, ObjectNode newField, String fieldName,
                                        String cristinFieldName) {
        if (input.has(fieldName)) {
            var nodeValue = input.get(fieldName);
            if (nodeValue.isNull()) {
                newField.putNull(cristinFieldName);
            } else {
                newField.put(cristinFieldName, nodeValue.asText());
            }
        }
    }

    private void addResponsibleInstitutionsIfPresent() {
        if (input.has(NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH)) {
            var responsibleInstitutions = input.get(NVA_INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH);
            if (responsibleInstitutions.isNull()) {
                output.putNull(INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH);
                return;
            }
            var cristinOrganizations = new ArrayList<CristinOrganization>();
            for (JsonNode responsibleInstitution : responsibleInstitutions) {
                var organization = extractOrganization(responsibleInstitution);
                var cristinOrganization = extractCristinOrganization(organization);
                cristinOrganizations.add(cristinOrganization);
            }

            output.set(INSTITUTIONS_RESPONSIBLE_FOR_RESEARCH, OBJECT_MAPPER.valueToTree(cristinOrganizations));
        }
    }

    private CristinOrganization extractCristinOrganization(Organization organization) {
        return fromOrganizationContainingUnitIfPresent(organization)
                   .orElse(fromOrganizationContainingInstitution(organization));
    }

    private Organization extractOrganization(JsonNode institution) {
        return new Organization.Builder().withId(URI.create(institution.get(ID).asText())).build();
    }

    private void addWebPageIfPresent() {
        if (input.has(WEB_PAGE)) {
            var webPage = input.get(WEB_PAGE);
            output.set(CRISTIN_EXTERNAL_URL, webPage);
        }
    }
}
