package no.unit.nva.model;

import java.net.URI;
import java.util.Map;

public interface Facet {

    URI getId();
    Integer getCount();
    String getKey();
    Map<String, String> getNames();
    Map<String, String> getLabels();

}
