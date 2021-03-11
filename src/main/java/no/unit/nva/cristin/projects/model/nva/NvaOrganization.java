package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.projects.JsonPropertyNames.ID;
import static no.unit.nva.cristin.projects.JsonPropertyNames.NAME;
import static no.unit.nva.cristin.projects.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
@JsonPropertyOrder({ID, TYPE, NAME})
public class NvaOrganization {

    private URI id;
    private String type;
    @JsonPropertyOrder(alphabetic = true)
    private Map<String, String> name;

    public URI getId() {
        return id;
    }

    public void setId(URI id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }
}
