package no.unit.nva.biobank.model.nva;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.unit.nva.biobank.model.cristin.CristinAssociatedProject;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

import java.beans.ConstructorProperties;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;
import static no.unit.nva.utils.UriUtils.createNvaProjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociatedProject implements JsonSerializable {

    private final URI id;
    private final Map<String,String> title;

    @JacocoGenerated
    @ConstructorProperties({ "id", "title" })
    @SuppressWarnings("unused")
    public AssociatedProject(URI id,  Map<String, String> title) {
        this.id = id;
        this.title = nonEmptyOrDefault(title);
    }

    public AssociatedProject(CristinAssociatedProject project) {
        this.id = createNvaProjectId(project.cristinProjectId());
        this.title = nonEmptyOrDefault(project.title());
    }

    public URI getId() {
        return id;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    @Override
    @JacocoGenerated
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssociatedProject that)) {
            return false;
        }
        return Objects.equals(getTitle(), that.getTitle())
               && Objects.equals(getId(), that.getId());
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getTitle(), getId());
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }
}
