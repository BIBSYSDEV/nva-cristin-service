package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.projects.model.cristin.CristinPerson;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.model.Organization;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CONTRIBUTORS;
import static no.unit.nva.cristin.model.JsonPropertyNames.COORDINATING_INSTITUTION;
import static no.unit.nva.cristin.model.JsonPropertyNames.END_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.LANGUAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.START_DATE;
import static no.unit.nva.cristin.model.JsonPropertyNames.TITLE;
import static no.unit.nva.cristin.projects.CristinOrganizationBuilder.fromOrganizationContainingInstitution;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
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
        return this;
    }

    private void addTitleAndLanguageIfBothPresent() {
        var language = getLanguageByUri(URI.create(input.get(LANGUAGE).asText()));
        var title = input.get(TITLE).asText();
        Map<String, String> titles = new ConcurrentHashMap<>();
        if (nonNull(language)) {
            titles.put(language.getIso6391Code(), title);
            output.set(TITLE, OBJECT_MAPPER.valueToTree(titles));
        }
    }

    private void addCoordinatingInstitutionIfPresent() {
        if (input.has(COORDINATING_INSTITUTION)) {
            final String content = input.get(COORDINATING_INSTITUTION).asText();
            Organization coordinatingInstitution =
                    attempt(() -> OBJECT_MAPPER.readValue(content, Organization.class)).orElseThrow();
            CristinOrganization cristinOrganization = fromOrganizationContainingInstitution(coordinatingInstitution);
            output.set(CRISTIN_COORDINATING_INSTITUTION, OBJECT_MAPPER.valueToTree(cristinOrganization));
        }
    }

    private void addContributorsIfPresent() {
        if (input.has(CONTRIBUTORS)) {
            TypeReference<List<NvaContributor>> typeRef = new TypeReference<>() {
            };
            List<NvaContributor> contributors =
                    attempt(() -> OBJECT_MAPPER.readValue(input.get(CONTRIBUTORS).asText(), typeRef)).orElseThrow();
            List<CristinPerson> participants = extractContributors(contributors);
            output.set(PARTICIPANTS, OBJECT_MAPPER.valueToTree(participants));
        }
    }

    private void addStartDateIfPresent() {
        if (input.has(START_DATE)) {
            output.set(START_DATE, input.get(START_DATE));
        }
    }

    private void addEndDateIfPresent() {
        if (input.has(END_DATE)) {
            output.set(END_DATE, input.get(END_DATE));
        }
    }

    public ObjectNode getOutput() {
        return output;
    }

    private static List<CristinPerson> extractContributors(List<NvaContributor> contributors) {
        return contributors.stream().map(NvaContributor::toCristinPersonWithRoles).collect(Collectors.toList());
    }
}
