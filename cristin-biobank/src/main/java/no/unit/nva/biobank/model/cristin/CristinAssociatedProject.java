package no.unit.nva.biobank.model.cristin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinAssociatedProject {

    @JsonProperty
    private String cristinProjectId;
    @JsonProperty
    private Map<String,String> title;
    @JsonProperty
    private URI url;

    public CristinAssociatedProject() {
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
        if (!(o instanceof CristinAssociatedProject)) {
            return false;
        }
        CristinAssociatedProject that = (CristinAssociatedProject) o;
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
        return new StringJoiner(", ", CristinAssociatedProject.class.getSimpleName() + "[", "]")
                   .add("cristinProjectId='" + cristinProjectId + "'")
                   .add("title=" + title)
                   .add("url=" + url)
                   .toString();
    }
}
