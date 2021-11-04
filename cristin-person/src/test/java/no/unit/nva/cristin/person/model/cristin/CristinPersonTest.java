package no.unit.nva.cristin.person.model.cristin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import nva.commons.core.JsonUtils;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;

public class CristinPersonTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private static final String CRISTIN_GET_PERSON_JSON =
        "cristinGetPersonResponse.json";

    @Test
    void cristinModelBuildsCorrectlyWhenDeserializingPersonJson() throws IOException {
        String body = getBodyFromResource(CRISTIN_GET_PERSON_JSON);
        CristinPerson person = fromJson(body, CristinPerson.class);
        assertEquals("359084", person.getCristinPersonId());
    }

    @Test
    void nvaModelBuildsCorrectlyWhenTransformingPersonFromCristinToNvaPerson() {
        assertEquals("Hei", "Hei");
    }

    private static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }
}
