package no.unit.nva.cristin.projects.model.nva;

import java.net.URI;
import java.util.Map;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class NvaOrganization {

    private URI id;
    private String type;
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
