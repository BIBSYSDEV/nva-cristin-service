package no.unit.nva.cristin.person.model.nva;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import org.junit.jupiter.api.Test;

public class PersonInstInfoPatchTest {

    public static final String DUMMY_EMAIL = "test@example.com";
    public static final String DUMMY_PHONE = "99112233";
    public static final String BODY_ALL_FIELDS =
        String.format("{\"email\":\"%s\", \"phone\":\"%s\"}", DUMMY_EMAIL, DUMMY_PHONE);
    public static final String BODY_FIELD_NULL = String.format("{\"email\": null, \"phone\":\"%s\"}", DUMMY_PHONE);
    public static final String BODY_FIELD_MISSING = String.format("{\"email\":\"%s\"}", DUMMY_EMAIL);

    @Test
    void shouldDeserializeSupportedFieldsIntoObject() throws JsonProcessingException {
        PersonInstInfoPatch info = deserializeBody(BODY_ALL_FIELDS);

        assertEquals(DUMMY_EMAIL, info.getEmail().orElseThrow());
        assertEquals(DUMMY_PHONE, info.getPhone().orElseThrow());
    }

    @Test
    void shouldDeserializeFieldContainingNullValueIntoObjectWithNullValue() throws JsonProcessingException {
        PersonInstInfoPatch info = deserializeBody(BODY_FIELD_NULL);

        assertThat(Objects.nonNull(info.getEmail()), equalTo(true));
        assertThat(info.getEmail().isEmpty(), equalTo(true));
    }

    @Test
    void shouldNotContainFieldAfterDeserializationWhenNotSentInPayload() throws JsonProcessingException {
        PersonInstInfoPatch info = deserializeBody(BODY_FIELD_MISSING);

        assertThat(info.getPhone(), equalTo(null));
    }

    private PersonInstInfoPatch deserializeBody(String body) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(body, PersonInstInfoPatch.class);
    }
}
