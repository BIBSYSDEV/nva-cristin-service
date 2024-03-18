package no.unit.nva.cristin.keyword.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.keyword.KeywordConstants;
import no.unit.nva.model.IdentifierWithLabels;
import no.unit.nva.utils.UriUtils;

public class Keyword extends IdentifierWithLabels implements JsonSerializable {

    public static final String type = "Keyword";

    @JsonCreator
    public Keyword(@JsonProperty(CONTEXT) String context,
                   @JsonProperty(TYPE) String type,
                   @JsonProperty(ID) URI id,
                   @JsonProperty(IDENTIFIER) String identifier,
                   @JsonProperty(LABELS) Map<String, String> labels) {

        super(context, type, id, identifier, labels);
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    // Builder
    public static class Builder {

        private String context;
        private URI id;
        private String identifier;
        private Map<String, String> labels;

        public Builder() {
        }

        public Builder withDefaultContext() {
            this.context = "https://bibsysdev.github.io/src/keyword-context.json";
            return this;
        }

        public Builder withContext(String context) {
            this.context = context;
            return this;
        }

        public Builder withIdentifier(String identifier) {
            this.identifier = identifier;
            this.id = UriUtils.getNvaApiId(identifier, KeywordConstants.KEYWORD_PATH);
            return this;
        }

        public Builder withLabels(Map<String, String> labels) {
            this.labels = labels;
            return this;
        }

        public Keyword build() {
            return new Keyword(context, type, id, identifier, labels);
        }
    }
}
