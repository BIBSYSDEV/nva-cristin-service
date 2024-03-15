package no.unit.nva.cristin.keyword.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import static no.unit.nva.cristin.model.JsonPropertyNames.LABELS;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;
import no.unit.nva.cristin.keyword.KeywordConstants;
import no.unit.nva.model.IdentifierWithLabels;
import no.unit.nva.utils.UriUtils;

public class Keyword extends IdentifierWithLabels implements JsonSerializable {

    public static final String context = "https://bibsysdev.github.io/src/keyword-context.json";
    public static final String type = "Keyword";

    @JsonCreator
    public Keyword(@JsonProperty(IDENTIFIER) String identifier,
                   @JsonProperty(LABELS) Map<String, String> labels) {
        super(context,
              type,
              generateId(identifier),
              identifier,
              labels);
    }

    private static URI generateId(String identifier) {
        return UriUtils.getNvaApiId(identifier, KeywordConstants.KEYWORD_PATH);
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
