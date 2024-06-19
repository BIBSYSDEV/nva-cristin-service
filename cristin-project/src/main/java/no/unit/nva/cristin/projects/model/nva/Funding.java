package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.Constants.TYPE;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;

import java.util.Objects;

public class Funding implements JsonSerializable {

    public static final String UNCONFIRMED_FUNDING = "UnconfirmedFunding";

    public static final String SOURCE = "source";
    public static final String IDENTIFIER = "identifier";
    public static final String LABELS = "labels";

    @JsonProperty(TYPE)
    private final String type;
    @JsonProperty(SOURCE)
    private final URI source;
    @JsonProperty(IDENTIFIER)
    private final String identifier;
    @JsonProperty(LABELS)
    private final Map<String, String> labels;

    /**
     * Default constructor.
     */
    @JsonCreator
    public Funding(@JsonProperty(TYPE) String type, @JsonProperty(SOURCE) URI source,
                   @JsonProperty(IDENTIFIER) String identifier,
                   @JsonProperty(LABELS) Map<String, String> labels) {
        this.type = type;
        this.source = source;
        this.identifier = identifier;
        this.labels = labels;
    }

    public String getType() {
        return type;
    }

    public URI getSource() {
        return source;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Funding funding)) {
            return false;
        }
        return Objects.equals(getType(), funding.getType())
               && Objects.equals(getSource(), funding.getSource())
               && Objects.equals(getIdentifier(), funding.getIdentifier())
               && Objects.equals(getLabels(), funding.getLabels());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getSource(), getIdentifier(), getLabels());
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
