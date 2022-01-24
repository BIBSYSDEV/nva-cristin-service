package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CristinProjectBuilder {

    private final transient CristinProject cristinProject;
    private final transient NvaProject nvaProject;


    public CristinProjectBuilder(NvaProject nvaProject) {
        this.nvaProject = nvaProject;
        this.cristinProject = new CristinProject();
    }

    public CristinProject build() {

        cristinProject.setCristinProjectId(extractLastPathElement(nvaProject.getId()));
        cristinProject.setMainLanguage(extractLastPathElement(nvaProject.getLanguage()));
        Map<String, String> title = new ConcurrentHashMap<>();
        title.put(extractLastPathElement(nvaProject.getLanguage()), nvaProject.getTitle());
        cristinProject.setTitle(title);
        cristinProject.setStatus(nvaProject.getStatus().name());
        //         String type;
        //         List<Map<String, String>> identifiers;
        //         String title;
        //         URI language;
        //         List<Map<String, String>> alternativeTitles;
        //         Instant startDate;
        //         Instant endDate;
        //         List<Funding> funding;
        //         Organization coordinatingInstitution;
        //         List<NvaContributor> contributors;
        //         ProjectStatus status;
        //         Map<String, String>  academicSummary;
        //         Map<String, String>  popularScientificSummary;

        return cristinProject;
    }

    private String extractLastPathElement(URI id) {
        return id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
    }
}
