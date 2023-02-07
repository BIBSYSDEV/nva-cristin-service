package no.unit.nva.cristin.projects.update;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import no.unit.nva.cristin.projects.model.cristin.CristinFundingSource;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.Funding;
import no.unit.nva.cristin.projects.model.nva.FundingSource;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.model.Organization;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingUnitIfPresent;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.FUNDING;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.model.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.cristin.projects.model.cristin.CristinProject.PROJECT_FUNDING_SOURCES;
import static no.unit.nva.cristin.projects.model.nva.Funding.SOURCE;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import static no.unit.nva.utils.CustomInstantSerializer.addMillisToInstantString;
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
        addTitleAndLanguageIfBothPresent();
        addCoordinatingInstitutionIfPresent();
        addContributorsIfPresent();
        addStartDateIfPresent();
        addEndDateIfPresent();
        addFundingIfPresent();
        return this;
    }

    private void addTitleAndLanguageIfBothPresent() {
        var language = getLanguageByUri(URI.create(input.get(LANGUAGE).asText()));
        if (nonNull(language)) {
            output.set(TITLE, OBJECT_MAPPER.valueToTree(Map.of(language.getIso6391Code(), input.get(TITLE).asText())));
        }
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
}
