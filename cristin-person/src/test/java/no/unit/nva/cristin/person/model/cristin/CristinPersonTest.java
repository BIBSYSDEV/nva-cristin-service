package no.unit.nva.cristin.person.model.cristin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import no.unit.nva.cristin.person.model.nva.Person;
import nva.commons.core.JsonUtils;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;

public class CristinPersonTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonUtils.dtoObjectMapper;
    private static final String CRISTIN_GET_PERSON_JSON =
        "cristinGetPersonResponse.json";
    private static final String NVA_API_GET_PERSON_JSON =
        "nvaApiGetPersonResponse.json";

    @Test
    void cristinModelBuildsCorrectlyWhenDeserializingPersonJson() throws IOException {
        String body = getBodyFromResource(CRISTIN_GET_PERSON_JSON);
        CristinPerson cristinPerson = fromJson(body, CristinPerson.class);
        JsonNode node = OBJECT_MAPPER.readTree(body);

        assertEquals(cristinPerson.getCristinPersonId(), node.get("cristin_person_id").asText());
        assertEquals(cristinPerson.getOrcid().getId(), node.get("orcid").get("id").asText());
        assertEquals(cristinPerson.getFirstName(), node.get("first_name").asText());
        assertEquals(cristinPerson.getSurname(), node.get("surname").asText());
        assertEquals(cristinPerson.getFirstNamePreferred(), node.get("first_name_preferred").asText());
        assertEquals(cristinPerson.getSurnamePreferred(), node.get("surname_preferred").asText());
        assertEquals(cristinPerson.getTel(), node.get("tel").asText());
        assertEquals(cristinPerson.getPictureUrl(), node.get("picture_url").asText());
        assertEquals(cristinPerson.getAffiliations().get(0).getActive(),
            node.get("affiliations").get(0).get("active").asBoolean());
        assertEquals(cristinPerson.getAffiliations().get(0).getUnit().getCristinUnitId(),
            node.get("affiliations").get(0).get("unit").get("cristin_unit_id").asText());
        assertEquals(cristinPerson.getAffiliations().get(0).getUnit().getUrl(),
            node.get("affiliations").get(0).get("unit").get("url").asText());
        assertEquals(cristinPerson.getAffiliations().get(1).getActive(),
            node.get("affiliations").get(1).get("active").asBoolean());
        assertEquals(cristinPerson.getAffiliations().get(1).getUnit().getCristinUnitId(),
            node.get("affiliations").get(1).get("unit").get("cristin_unit_id").asText());
        assertEquals(cristinPerson.getAffiliations().get(1).getUnit().getUrl(),
            node.get("affiliations").get(1).get("unit").get("url").asText());
    }

    @Test
    void nvaModelBuildsCorrectlyWhenTransformingPersonFromCristinToNvaPerson() throws IOException {
        String cristinBody = getBodyFromResource(CRISTIN_GET_PERSON_JSON);
        CristinPerson cristinPerson = fromJson(cristinBody, CristinPerson.class);

        String nvaBody = getBodyFromResource(NVA_API_GET_PERSON_JSON);
        Person nvaPerson = fromJson(nvaBody, Person.class);

        // TODO: Compare nvaPerson created from Json with nvaPerson built from cristinPerson

        //assertEquals("", OBJECT_MAPPER.writeValueAsString(nvaPerson));
    }

    private static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }
}
