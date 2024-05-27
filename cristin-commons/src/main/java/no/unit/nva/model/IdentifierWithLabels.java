package no.unit.nva.model;

import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;

public class IdentifierWithLabels {

    @JsonProperty(CONTEXT)
    private final String context;
    @JsonProperty(TYPE)
    private final String type;
    @JsonProperty(ID)
    private final URI id;
    @JsonProperty(IDENTIFIER)
    private final String identifier;
    @JsonProperty(LABELS)
    private final Map<String, String> labels;

    public IdentifierWithLabels(@JsonProperty(CONTEXT) String context,
                                @JsonProperty(TYPE) String type,
                                @JsonProperty(ID) URI id,
                                @JsonProperty(IDENTIFIER) String identifier,
                                @JsonProperty(LABELS) Map<String, String> labels) {
        this.context = context;
        this.type = type;
        this.id = id;
        this.identifier = identifier;
        this.labels = labels;
    }

    public String getContext() {
        return context;
    }

    public String getType() {
        return type;
    }

    public URI getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    // Builder
    @SuppressWarnings("unused")
    public static class Builder {

        private String context;
        private String type;
        private URI id;
        private String identifier;
        private Map<String, String> labels;

        public Builder withContext(String context) {
            this.context = context;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withId(URI id) {
            this.id = id;
            return this;
        }

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public IdentifierWithLabels build() {
            return new IdentifierWithLabels(context, type, id, identifier, labels);
        }
    }

}
