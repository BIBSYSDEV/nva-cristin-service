package no.unit.nva.biobank.model.nva;

import static no.unit.nva.utils.UriUtils.createNvaProjectId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.biobank.model.cristin.CristinAssociatedProject;
import no.unit.nva.commons.json.JsonSerializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociatedProject implements JsonSerializable {

    @JsonProperty
    private String cristinProjectId;
    @JsonProperty
    private Map<String,String> title;
    @JsonProperty
    private URI url;

    public AssociatedProject() {
    }

    public AssociatedProject(CristinAssociatedProject project) {
        this.cristinProjectId = project.getCristinProjectId();
        this.title = project.getTitle();
        this.url = createNvaProjectId(cristinProjectId);
    }

    public String getCristinProjectId() {
        return cristinProjectId;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public URI getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssociatedProject)) {
            return false;
        }
        AssociatedProject that = (AssociatedProject) o;
        return Objects.equals(getCristinProjectId(), that.getCristinProjectId())
               && Objects.equals(getTitle(), that.getTitle())
               && Objects.equals(getUrl(), that.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCristinProjectId(), getTitle(), getUrl());
    }

    @Override
    public String toString() {
        return this.toJsonString();
    }
}
