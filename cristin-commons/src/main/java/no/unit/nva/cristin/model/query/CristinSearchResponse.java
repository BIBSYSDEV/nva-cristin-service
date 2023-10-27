package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public interface CristinSearchResponse<T> {

    @JsonProperty("data")
    @JsonGetter
    T data();

    @JsonProperty("facets")
    @JsonGetter
    Map<String, CristinFacet[]> facets();

}
