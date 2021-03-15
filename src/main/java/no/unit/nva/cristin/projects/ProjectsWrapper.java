package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ProjectsWrapper {

    @JsonProperty("@context")
    public String context;
    public String id;
    public Integer numberOfResults;
    public String query;
    public String processingTime;
    public Integer firstRecord;
    public Integer totalRecords;
    public List<ProjectPresentation> hits;
}
