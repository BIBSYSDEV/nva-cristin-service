package no.unit.nva.cristin.model.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.model.Facet;

@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
public record CristinFacetAdapter(@JsonIgnore CristinFacet cristinFacet) implements Facet, JsonSerializable {

    @Override
    public URI getId() {
        return null;
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
