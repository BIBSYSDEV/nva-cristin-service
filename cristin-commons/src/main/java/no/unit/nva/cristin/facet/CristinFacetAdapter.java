package no.unit.nva.cristin.facet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.model.query.CristinFacet;
import no.unit.nva.facet.Facet;

@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
public record CristinFacetAdapter(@JsonIgnore CristinFacet cristinFacet,
                                  @JsonIgnore URI id) implements Facet, JsonSerializable {

    @Override
    public URI getId() {
        return id;
    }

    @Override
    public Integer getCount() {
        return cristinFacet.getCount();
    }

    @Override
    public String getKey() {
        return cristinFacet.getKey();
    }

    @Override
    public Map<String, String> getNames() {
        return null;
    }

    @Override
    public Map<String, String> getLabels() {
        return cristinFacet.getLabels();
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
