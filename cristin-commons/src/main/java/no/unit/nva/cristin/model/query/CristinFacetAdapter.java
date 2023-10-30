package no.unit.nva.cristin.model.query;

import java.net.URI;
import java.util.Map;
import no.unit.nva.model.Facet;

@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
public record CristinFacetAdapter(CristinFacet cristinFacet) implements Facet {

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
}
