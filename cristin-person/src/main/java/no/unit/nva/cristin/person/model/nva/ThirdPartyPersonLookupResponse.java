package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.CONTEXT;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.TYPE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NAMES;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URI;
import java.util.Set;
import no.unit.nva.commons.json.JsonSerializable;

/**
 * Response to a third-party client looking up a person by national identity number. It only holds
 * what a third party needs to find the person in Cristin: the id and the names. Everything else in
 * {@link Person} is left out on purpose, so third-party access stays separate from GUI access.
 */
@JsonPropertyOrder({CONTEXT, ID, TYPE, NAMES})
public record ThirdPartyPersonLookupResponse(
    @JsonProperty(CONTEXT) String context,
    @JsonProperty(ID) URI id,
    @JsonProperty(NAMES) Set<TypedValue> names)
    implements JsonSerializable, PersonLookupResult {

  /**
   * Creates the third-party response from a full person.
   *
   * <p>Names are included even when the person is reserved. A third party can only look a person up
   * when it already has the person's national identity number, so the names do not reveal anything
   * about whom the person is.
   *
   * @param person the full person as fetched from Cristin
   * @return the response holding only id and names
   */
  public static ThirdPartyPersonLookupResponse fromPerson(Person person) {
    return new ThirdPartyPersonLookupResponse(person.context(), person.id(), person.names());
  }

  @JsonProperty(TYPE)
  public String type() {
    return Person.type;
  }

  @Override
  public URI getId() {
    return id;
  }
}
