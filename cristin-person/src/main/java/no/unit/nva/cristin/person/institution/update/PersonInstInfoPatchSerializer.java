package no.unit.nva.cristin.person.institution.update;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Objects;
import no.unit.nva.cristin.person.model.nva.PersonInstInfoPatch;

public class PersonInstInfoPatchSerializer extends StdSerializer<PersonInstInfoPatch> {

    public static final String EMAIL_FIELD = "email";
    public static final String PHONE_FIELD = "phone";

    public PersonInstInfoPatchSerializer() {
        this(null);
    }

    public PersonInstInfoPatchSerializer(Class<PersonInstInfoPatch> t) {
        super(t);
    }

    @Override
    public void serialize(
        PersonInstInfoPatch value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException {

        jgen.writeStartObject();
        if (Objects.nonNull(value.getEmail())) {
            jgen.writeStringField(EMAIL_FIELD, value.getEmail().orElse(null));
        }
        if (Objects.nonNull(value.getPhone())) {
            jgen.writeStringField(PHONE_FIELD, value.getPhone().orElse(null));
        }
        jgen.writeEndObject();
    }
}
