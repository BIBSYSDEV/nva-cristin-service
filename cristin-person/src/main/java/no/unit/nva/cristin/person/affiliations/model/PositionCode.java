package no.unit.nva.cristin.person.affiliations.model;

import static no.unit.nva.cristin.common.Utils.nonEmptyOrDefault;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Map;
import no.unit.nva.commons.json.JsonSerializable;

public record PositionCode(
    @JsonProperty("id") URI id,
    @JsonProperty("labels") Map<String, String> labels,
    @JsonProperty("enabled") boolean enabled)
    implements JsonSerializable {

  @Override
  public Map<String, String> labels() {
    return nonEmptyOrDefault(labels);
  }

  @Override
  public String toString() {
    return toJsonString();
  }
}
