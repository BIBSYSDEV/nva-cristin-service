package no.unit.nva.biobank.model.nva;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.unit.nva.biobank.model.cristin.CristinAssociatedProject;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

import java.beans.ConstructorProperties;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

import static no.unit.nva.utils.UriUtils.createNvaProjectId;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociatedProject implements JsonSerializable {

    private final String cristinProjectId;
    private final Map<String,String> title;
    private final URI url;

    @ConstructorProperties({"cristinProjectId","title","url"})
    public AssociatedProject(String cristinProjectId, Map<String, String> title, URI url) {
        this.cristinProjectId = cristinProjectId;
        this.title = title;
        this.url = url;
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
    @JacocoGenerated
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
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(getCristinProjectId(), getTitle(), getUrl());
    }

    @Override
    @JacocoGenerated
    public String toString() {
        return this.toJsonString();
    }
}
