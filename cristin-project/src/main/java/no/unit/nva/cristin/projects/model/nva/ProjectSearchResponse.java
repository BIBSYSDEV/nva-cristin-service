package no.unit.nva.cristin.projects.model.nva;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import no.unit.nva.cristin.common.model.SearchResponse;

public class ProjectSearchResponse extends SearchResponse {

    @JsonProperty
    private List<NvaProject> hits;

    @Override
    public List<NvaProject> getHits() {
        return hits;
    }

    public void setHits(List<NvaProject> hits) {
        this.hits = hits;
    }

    public ProjectSearchResponse withHits(List<NvaProject> hits) {
        this.hits = hits;
        return this;
    }
}
