package no.unit.nva.cristin.organization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;

import java.net.URI;

public class CorrespondingUnitDto {

    private String id;
    private URI uri;

    public CorrespondingUnitDto(@JsonProperty("cristin_unit_id") String id, @JsonProperty("url") String uri) {
        this.id = id;
        this.uri = URI.create(uri);
    }

    @JacocoGenerated
    public String getId() {
        return id;
    }

    @JacocoGenerated
    public void setId(String id) {
        this.id = id;
    }

    @JacocoGenerated
    public URI getUri() {
        return uri;
    }

    @JacocoGenerated
    public void setUri(URI uri) {
        this.uri = uri;
    }
}
