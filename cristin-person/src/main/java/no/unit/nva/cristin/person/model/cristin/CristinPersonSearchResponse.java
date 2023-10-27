package no.unit.nva.cristin.person.model.cristin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import no.unit.nva.cristin.model.CristinFacet;
import no.unit.nva.cristin.model.CristinPerson;
import no.unit.nva.cristin.model.CristinSearchResponse;

public record CristinPersonSearchResponse(CristinPerson[] data,
                                          Map<String, CristinFacet[]> facets)
    implements CristinSearchResponse<CristinPerson[]> {

    @JsonCreator
    public CristinPersonSearchResponse(@JsonProperty("data") CristinPerson[] data,
                                       @JsonProperty("facets") Map<String, CristinFacet[]> facets) {
        this.data = data.clone();
        this.facets = facets;
    }

}
