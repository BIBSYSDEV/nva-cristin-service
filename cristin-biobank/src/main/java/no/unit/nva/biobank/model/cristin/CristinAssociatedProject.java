package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.commons.json.JsonSerializable;
import nva.commons.core.JacocoGenerated;

import java.beans.ConstructorProperties;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinAssociatedProject implements JsonSerializable {

    private final String cristinProjectId;
    private final Map<String, String> title;
    private final URI url;

    @ConstructorProperties({"cristinProjectId", "title", "url"})
    public CristinAssociatedProject(String cristinProjectId, Map<String, String> title, URI url) {
        this.cristinProjectId = cristinProjectId;
        this.title = title;
        this.url = url;
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
        if (!(o instanceof CristinAssociatedProject)) {
            return false;
        }
        CristinAssociatedProject that = (CristinAssociatedProject) o;
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
