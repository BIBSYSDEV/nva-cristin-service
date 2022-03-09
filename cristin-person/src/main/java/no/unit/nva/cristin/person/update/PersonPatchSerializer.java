package no.unit.nva.cristin.person.update;

import static java.util.Objects.nonNull;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Map;
import no.unit.nva.cristin.person.model.nva.PersonPatch;

public class PersonPatchSerializer extends StdSerializer<PersonPatch> {

    public static final String ORCID_FIELD = "orcid";
    public static final String ORCID_IDENTIFIER_FIELD = "id";
    public static final String FIRSTNAME_FIELD = "first_name";
    public static final String LASTNAME_FIELD = "surname";
    public static final String PREFERRED_FIRSTNAME_FIELD = "first_name_preferred";
    public static final String PREFERRED_LASTNAME_FIELD = "surname_preferred";
    public static final String RESERVED_FIELD = "reserved";

    public PersonPatchSerializer() {
        this(null);
    }

    public PersonPatchSerializer(Class<PersonPatch> t) {
        super(t);
    }

    @Override
    public void serialize(PersonPatch value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();

        if (nonNull(value.getOrcid())) {
            if (value.getOrcid().isPresent()) {
                Map<String, String> orcid = Map.of(ORCID_IDENTIFIER_FIELD, value.getOrcid().get());
                generator.writeObjectField(ORCID_FIELD, orcid);
            } else {
                generator.writeStringField(ORCID_FIELD, null);
            }
        }
        if (nonNull(value.getFirstName())) {
            generator.writeStringField(FIRSTNAME_FIELD, value.getFirstName().orElse(null));
        }
        if (nonNull(value.getLastName())) {
            generator.writeStringField(LASTNAME_FIELD, value.getLastName().orElse(null));
        }
        if (nonNull(value.getPreferredFirstName())) {
            generator.writeStringField(PREFERRED_FIRSTNAME_FIELD, value.getPreferredFirstName().orElse(null));
        }
        if (nonNull(value.getPreferredLastName())) {
            generator.writeStringField(PREFERRED_LASTNAME_FIELD, value.getPreferredLastName().orElse(null));
        }
        if (nonNull(value.getReserved()) && value.getReserved().isPresent()) {
            generator.writeBooleanField(RESERVED_FIELD, value.getReserved().get());
        }

        generator.writeEndObject();
    }
}
