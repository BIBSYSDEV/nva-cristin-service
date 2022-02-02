package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.institution.update.PersonInstInfoPatchSerializer.EMAIL_FIELD;
import static no.unit.nva.cristin.person.institution.update.PersonInstInfoPatchSerializer.PHONE_FIELD;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class PersonInstInfoPatchTest {

    public static final String DUMMY_EMAIL = "test@example.com";
    public static final String DUMMY_PHONE = "99112233";
    public static final String BODY_ALL_FIELDS =
        String.format("{\"email\":\"%s\", \"phone\":\"%s\"}", DUMMY_EMAIL, DUMMY_PHONE);
    public static final String BODY_EMAIL_VALUE_NULL =
        String.format("{\"email\": null, \"phone\":\"%s\"}", DUMMY_PHONE);
    public static final String BODY_PHONE_FIELD_MISSING = String.format("{\"email\":\"%s\"}", DUMMY_EMAIL);

    static Stream<String> jsonPayloads() {
        return Stream.of(BODY_ALL_FIELDS, BODY_EMAIL_VALUE_NULL, BODY_PHONE_FIELD_MISSING);
    }

    @ParameterizedTest
    @MethodSource("jsonPayloads")
    void shouldKeepOriginalPayloadWhenDoingBothSerializationAndDeserialization(String payload)
        throws JsonProcessingException {

        PersonInstInfoPatch input = deserializeBody(payload);
        String serializedInput = OBJECT_MAPPER.writeValueAsString(input);
        PersonInstInfoPatch output = deserializeBody(serializedInput);

        assertThat(output, equalTo(input));

        JsonNode inputNode = OBJECT_MAPPER.readTree(payload);
        JsonNode outputNode = OBJECT_MAPPER.readTree(serializedInput);

        assertThat(outputNode, equalTo(inputNode));
    }

    @Test
    void shouldPreserveNullValueFromInputWhenSerializing() throws JsonProcessingException {
        PersonInstInfoPatch input = deserializeBody(BODY_EMAIL_VALUE_NULL);
        String serializedPayload = OBJECT_MAPPER.writeValueAsString(input);

        JsonNode node = OBJECT_MAPPER.readTree(serializedPayload);
        assertThat(node.has(EMAIL_FIELD), equalTo(true));
        assertThat(node.get(EMAIL_FIELD).isNull(), equalTo(true));
    }

    @Test
    void shouldNotSerializeFieldsIfTheyAreNotIncludedInClientPayload() throws JsonProcessingException {
        PersonInstInfoPatch input = deserializeBody(BODY_PHONE_FIELD_MISSING);
        String serializedPayload = OBJECT_MAPPER.writeValueAsString(input);
        JsonNode node = OBJECT_MAPPER.readTree(serializedPayload);

        assertThat(node.has(PHONE_FIELD), equalTo(false));
        assertThat(node.has(EMAIL_FIELD), equalTo(true));
    }

    private PersonInstInfoPatch deserializeBody(String body) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(body, PersonInstInfoPatch.class);
    }
}
