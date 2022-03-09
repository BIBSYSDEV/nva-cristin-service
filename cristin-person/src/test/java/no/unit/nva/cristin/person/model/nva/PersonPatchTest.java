package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.update.PersonPatchSerializer.FIRSTNAME_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchSerializer.LASTNAME_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchSerializer.ORCID_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchSerializer.ORCID_IDENTIFIER_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchSerializer.PREFERRED_FIRSTNAME_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchSerializer.PREFERRED_LASTNAME_FIELD;
import static no.unit.nva.cristin.person.update.PersonPatchSerializer.RESERVED_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class PersonPatchTest {

    private static final String SOME_ORCID = "1234-1234-1234-1234";
    private static final String SOME_FIRSTNAME = "Henrik";
    private static final String SOME_LASTNAME = "Olsen";
    private static final String SOME_PREFERRED_FIRSTNAME = "Ole-Henrik";
    private static final String SOME_PREFERRED_LASTNAME = "Olsen-Hagen";

    private static final String bodyWithOrcidAndPrimaryNamesAndNullPreferredName =
        "{\"orcid\":\"1234-1234-1234-1234\",\"firstName\":\"Henrik\", \"lastName\":\"Olsen\", "
            + "\"preferredFirstName\":null}";
    private static final String bodyWithNullOrcid = "{\"orcid\":null,\"firstName\":\"Henrik\"}";
    private static final String bodyWithPreferredNames = "{\"preferredFirstName\":\"Ole-Henrik\","
        + "\"preferredLastName\":\"Olsen-Hagen\"}";
    private static final String bodyWithPreferredNamesAsNull = "{\"preferredFirstName\":null,"
        + "\"preferredLastName\":null}";
    private static final String bodyWithReservedField =
        "{\"firstName\":\"Henrik\", \"reserved\":true}";
    private static final String bodyWithOnlyFirstNameField = "{\"firstName\":\"Henrik\"}";

    @Test
    void shouldSerializeOrcidBothWithValueAndAsNull() throws JsonProcessingException {
        String serializedStringWithOrcid =
            OBJECT_MAPPER.writeValueAsString(readBody(bodyWithOrcidAndPrimaryNamesAndNullPreferredName));
        JsonNode jsonNode = OBJECT_MAPPER.readTree(serializedStringWithOrcid);

        assertEquals(SOME_ORCID, jsonNode.get(ORCID_FIELD).get(ORCID_IDENTIFIER_FIELD).asText());

        String serializedStringWithoutOrcid = OBJECT_MAPPER.writeValueAsString(readBody(bodyWithNullOrcid));
        JsonNode jsonNodeWithNullOrcid = OBJECT_MAPPER.readTree(serializedStringWithoutOrcid);

        assertThat(jsonNodeWithNullOrcid.has(ORCID_FIELD), equalTo(true));
        assertThat(jsonNodeWithNullOrcid.get(ORCID_FIELD).isNull(), equalTo(true));
    }

    @Test
    void shouldSerializePrimaryNamesToCorrectFields() throws JsonProcessingException {
        String serializedStringWithFirstName =
            OBJECT_MAPPER.writeValueAsString(readBody(bodyWithOrcidAndPrimaryNamesAndNullPreferredName));
        JsonNode jsonNode = OBJECT_MAPPER.readTree(serializedStringWithFirstName);

        assertEquals(SOME_FIRSTNAME, jsonNode.get(FIRSTNAME_FIELD).asText());
        assertEquals(SOME_LASTNAME, jsonNode.get(LASTNAME_FIELD).asText());
    }

    @Test
    void shouldSerializePreferredNamesToCorrectFieldsBothIfHasValuesOrIsNull() throws JsonProcessingException {
        String serializedStringWithValues = OBJECT_MAPPER.writeValueAsString(readBody(bodyWithPreferredNames));
        JsonNode jsonNodeWithValues = OBJECT_MAPPER.readTree(serializedStringWithValues);

        assertEquals(SOME_PREFERRED_FIRSTNAME, jsonNodeWithValues.get(PREFERRED_FIRSTNAME_FIELD).asText());
        assertEquals(SOME_PREFERRED_LASTNAME, jsonNodeWithValues.get(PREFERRED_LASTNAME_FIELD).asText());

        String serializedStringWithNulls = OBJECT_MAPPER.writeValueAsString(readBody(bodyWithPreferredNamesAsNull));
        JsonNode jsonNodeWithNulls = OBJECT_MAPPER.readTree(serializedStringWithNulls);

        assertThat(jsonNodeWithNulls.has(PREFERRED_FIRSTNAME_FIELD), equalTo(true));
        assertThat(jsonNodeWithNulls.has(PREFERRED_LASTNAME_FIELD), equalTo(true));
        assertThat(jsonNodeWithNulls.get(PREFERRED_FIRSTNAME_FIELD).isNull(), equalTo(true));
        assertThat(jsonNodeWithNulls.get(PREFERRED_LASTNAME_FIELD).isNull(), equalTo(true));
    }

    @Test
    void shouldNotSerializeFieldsIfTheyAreNotSentInPayload() throws JsonProcessingException {
        String serializedString = OBJECT_MAPPER.writeValueAsString(readBody(bodyWithOnlyFirstNameField));
        JsonNode jsonNode = OBJECT_MAPPER.readTree(serializedString);
        Set<String> fieldsThatShouldNotBePresent =
            Set.of(LASTNAME_FIELD, ORCID_FIELD, PREFERRED_FIRSTNAME_FIELD, PREFERRED_LASTNAME_FIELD, RESERVED_FIELD);

        assertThat(fieldsThatShouldNotBePresent.stream().noneMatch(jsonNode::has), equalTo(true));
    }

    @Test
    void shouldSerializeReservedIfPresent() throws JsonProcessingException {
        String serializedString = OBJECT_MAPPER.writeValueAsString(readBody(bodyWithReservedField));
        JsonNode jsonNode = OBJECT_MAPPER.readTree(serializedString);

        assertThat(jsonNode.has(RESERVED_FIELD), equalTo(true));
        assertThat(jsonNode.get(RESERVED_FIELD).asBoolean(), equalTo(true));
    }

    private PersonPatch readBody(String body) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(body, PersonPatch.class);
    }
}
