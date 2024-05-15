package no.unit.nva.cristin.projects.model.cristin.query;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import no.unit.nva.cristin.model.query.CristinFacet;
import no.unit.nva.cristin.model.query.CristinSearchResponse;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;

public record CristinProjectSearchResponse(CristinProject[] data,
                                           Map<String, CristinFacet[]> facets)
    implements CristinSearchResponse<CristinProject[]> {

    @JsonCreator
    public CristinProjectSearchResponse(@JsonProperty("data") CristinProject[] data,
                                       @JsonProperty("facets") Map<String, CristinFacet[]> facets) {
        this.data = nonNull(data) ? data.clone() : new CristinProject[0];
        this.facets = facets;
    }

}
