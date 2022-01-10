package no.unit.nva.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import no.unit.nva.cristin.common.Utils;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.JsonSerializable;

@SuppressWarnings("unused")
@JacocoGenerated
public class Context implements JsonSerializable {

    @JsonProperty("@vocab")
    private final URI vocab;
    private final String id;
    @JsonProperty("@base")
    private final URI base;
    private final String type;
    private final Map<String, String> label;

    /**
     * Creates a Context for serialization to client with some default values.
     */
    public Context(URI vocab, URI base) {
        this(vocab, "@id", base, "@type", Map.of("@container", "@language"));
    }

    /**
     * Creates a Context for serialization to client.
     */
    @JsonCreator
    public Context(@JsonProperty("@vocab") URI vocab, @JsonProperty("id") String id, @JsonProperty("@base") URI base,
                   @JsonProperty("type") String type, @JsonProperty("label") Map<String, String> label) {
        this.vocab = vocab;
        this.id = id;
        this.base = base;
        this.type = type;
        this.label = label;
    }

    public URI getVocab() {
        return vocab;
    }

    public String getId() {
        return id;
    }

    public URI getBase() {
        return base;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getLabel() {
        return Utils.nonEmptyOrDefault(label);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Context)) {
            return false;
        }
        Context that = (Context) o;
        return Objects.equals(getVocab(), that.getVocab())
            && Objects.equals(getId(), that.getId())
            && Objects.equals(getBase(), that.getBase())
            && Objects.equals(getType(), that.getType())
            && getLabel().equals(that.getLabel());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @JacocoGenerated
    @Override
    public int hashCode() {
        return Objects.hash(getVocab(), getId(), getBase(), getType(), getLabel());
    }
}
