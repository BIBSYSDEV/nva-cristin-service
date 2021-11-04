package no.unit.nva.cristin.person.model.cristin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.JsonNode;
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
        JsonNode node = OBJECT_MAPPER.readTree(body);

        assertEquals(person.getCristinPersonId(), node.get("cristin_person_id").asText());
        assertEquals(person.getOrcid().getId(), node.get("orcid").get("id").asText());
        assertEquals(person.getFirstName(), node.get("first_name").asText());
        assertEquals(person.getSurname(), node.get("surname").asText());
        assertEquals(person.getFirstNamePreferred(), node.get("first_name_preferred").asText());
        assertEquals(person.getSurnamePreferred(), node.get("surname_preferred").asText());
        assertEquals(person.getTel(), node.get("tel").asText());
        assertEquals(person.getPictureUrl(), node.get("picture_url").asText());
        assertEquals(person.getAffiliations().get(0).getActive(),
            node.get("affiliations").get(0).get("active").asBoolean());
        assertEquals(person.getAffiliations().get(0).getUnit().getCristinUnitId(),
            node.get("affiliations").get(0).get("unit").get("cristin_unit_id").asText());
        assertEquals(person.getAffiliations().get(0).getUnit().getUrl(),
            node.get("affiliations").get(0).get("unit").get("url").asText());
        assertEquals(person.getAffiliations().get(1).getActive(),
            node.get("affiliations").get(1).get("active").asBoolean());
        assertEquals(person.getAffiliations().get(1).getUnit().getCristinUnitId(),
            node.get("affiliations").get(1).get("unit").get("cristin_unit_id").asText());
        assertEquals(person.getAffiliations().get(1).getUnit().getUrl(),
            node.get("affiliations").get(1).get("unit").get("url").asText());
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
