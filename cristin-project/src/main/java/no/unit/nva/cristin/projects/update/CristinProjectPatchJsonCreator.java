package no.unit.nva.cristin.projects.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.cristin.CristinTypedLabel;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.language.Language;
import no.unit.nva.model.Organization;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingUnitIfPresent;
import static no.unit.nva.cristin.model.JsonPropertyNames.ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_ACADEMIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.POPULAR_SCIENTIFIC_SUMMARY;
import static no.unit.nva.cristin.model.JsonPropertyNames.PROJECT_CATEGORIES;
import static no.unit.nva.cristin.model.JsonPropertyNames.RELATED_PROJECTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_PROJECT_CATEGORIES;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.CRISTIN_RELATED_PROJECTS;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.EQUIPMENT;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.KEYWORDS;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.METHOD;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.PROJECT_FUNDING_SOURCES;
import static no.unit.nva.cristin.projects.model.nva.Funding.SOURCE;
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
        return this;
    }

    private void addTitleAndLanguageIfPresent() {
        if (input.has(LANGUAGE)) {
            var language = getLanguageByUri(URI.create(input.get(LANGUAGE).asText()));
            var title = input.get(TITLE);
            if (nonNull(language) && nonNull(title)) {
                updateLanguage(language, title);
            } else if (nonNull(language) && isSupportedLanguage(language)) {
                eraseLanguage(language);
            }
        }
    }

    private void updateLanguage(Language language, JsonNode title) {
        output.set(TITLE, OBJECT_MAPPER.valueToTree(Map.of(language.getIso6391Code(), title.asText())));
    }

    private void eraseLanguage(Language language) {
        var languageToBeErased = OBJECT_MAPPER.createObjectNode().putNull(language.getIso6391Code());
        output.set(TITLE, languageToBeErased);
    }

    private boolean isSupportedLanguage(Language language) {
        return !UNDEFINED_LANGUAGE.equals(language);
    }

    private void addCoordinatingInstitutionIfPresent() {
        if (input.has(COORDINATING_INSTITUTION)) {
            var coordinatingInstitution =
                    attempt(() -> OBJECT_MAPPER.readValue(input.get(COORDINATING_INSTITUTION).toString(),
                            Organization.class))
                            .orElseThrow();

            var cristinOrganization = fromOrganizationContainingUnitIfPresent(coordinatingInstitution)
                                          .orElse(fromOrganizationContainingInstitution(coordinatingInstitution));
            output.set(CRISTIN_COORDINATING_INSTITUTION, OBJECT_MAPPER.valueToTree(cristinOrganization));
        }
    }

    private void addContributorsIfPresent() {
        if (input.has(CONTRIBUTORS)) {
            TypeReference<List<NvaContributor>> typeRef = new TypeReference<>() {
            };
            var contributors =
                    attempt(() -> OBJECT_MAPPER.readValue(input.get(CONTRIBUTORS).toString(), typeRef)).orElseThrow();
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
        return contributors.stream().map(NvaContributor::toCristinPersonWithRoles).collect(Collectors.toList());
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
        var sourceCode = fundingSource.get(SOURCE).get(FundingSource.CODE).asText();
        cristinFundingSource.setFundingSourceCode(sourceCode);
        if (fundingSource.has(Funding.CODE)) {
            var projectCode = fundingSource.get(Funding.CODE).asText();
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

}
