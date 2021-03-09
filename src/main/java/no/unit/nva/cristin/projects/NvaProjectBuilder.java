package no.unit.nva.cristin.projects;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaContributor;
import no.unit.nva.cristin.projects.model.nva.NvaOrganization;
import no.unit.nva.cristin.projects.model.nva.NvaPerson;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class NvaProjectBuilder {

    private static final String PROJECT_CONTEXT = "https://example.org/search-api-context.json";
    private static final String NVA_PROJECT_URL = "https://sandbox.nva.unit.no/project/";
    private static final String CRISTIN_INSTITUTION_BASE_URI = "https://api.cristin.no/v2/institutions/";
    private static final String CRISTIN_PERSON_BASE_URL = "https://api.cristin.no/v2/persons/";
    private static final String TEMPORARY_LANGUAGE_URL = "https://lexvo.org/id/iso639-3/nno";

    private static final String PROJECT_TYPE = "Project";
    private static final String CRISTIN_IDENTIFIER = "CristinIdentifier";
    private static final String ORGANIZATION = "Organization";
    private static final String PERSON = "Person";

    private static final String TYPE = "type";
    private static final String VALUE = "value";

    private static final Map<String, String> cristinRolesToNva = Map.of("PRO_MANAGER", "ProjectManager",
        "PRO_PARTICIPANT", "ProjectParticipant");

    public static NvaProject cristinProjectToNvaProject(CristinProject cristinProject) throws URISyntaxException {
        NvaProject nvaProject = new NvaProject();

        nvaProject.setContext(PROJECT_CONTEXT);
        nvaProject.setId(new URI(NVA_PROJECT_URL + cristinProject.cristinProjectId));
        nvaProject.setType(PROJECT_TYPE);
        nvaProject.setIdentifier(
            Collections.singletonList(Map.of(TYPE, CRISTIN_IDENTIFIER, VALUE, cristinProject.cristinProjectId)));
        nvaProject.setTitle(cristinProject.title.get(cristinProject.mainLanguage));
        nvaProject.setLanguage(new URI(TEMPORARY_LANGUAGE_URL));
        nvaProject.setAlternativeTitles(Collections.singletonList(
            cristinProject.title.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(cristinProject.mainLanguage))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
        nvaProject.setStartDate(cristinProject.startDate);
        nvaProject.setEndDate(cristinProject.endDate);

        NvaOrganization nvaOrganization = new NvaOrganization();
        nvaOrganization.setId(new URI(CRISTIN_INSTITUTION_BASE_URI
            + cristinProject.coordinatingInstitution.institution.cristinInstitutionId));
        nvaOrganization.setType(ORGANIZATION);
        nvaOrganization.setName(cristinProject.coordinatingInstitution.institution.institutionName);
        nvaProject.setCoordinatingInstitution(nvaOrganization);

        List<NvaContributor> nvaContributors = new ArrayList<>();
        cristinProject.participants.forEach(person -> {
            person.roles.forEach(role -> {
                NvaContributor nvaContributor = new NvaContributor();
                nvaContributor.setType(cristinRolesToNva.get(role.roleCode));

                NvaPerson identity = new NvaPerson();
                identity.setId(attempt(() -> new URI(CRISTIN_PERSON_BASE_URL + person.cristinPersonId)).get());
                identity.setType(PERSON);
                identity.setFirstName(person.firstName);
                identity.setLastName(person.surname);
                nvaContributor.setIdentity(identity);

                NvaOrganization affiliation = new NvaOrganization();
                affiliation.setId(attempt(() ->
                    new URI(CRISTIN_INSTITUTION_BASE_URI + role.institution.cristinInstitutionId)).get());
                affiliation.setType(ORGANIZATION);
                affiliation.setName(role.institution.institutionName);
                nvaContributor.setAffiliation(affiliation);

                nvaContributors.add(nvaContributor);
            });
        });
        nvaProject.setContributors(nvaContributors);

        return nvaProject;
    }
}
