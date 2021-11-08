package no.unit.nva.cristin.person.model.cristin;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

        ObjectNode bodyNode = removeIgnoredFields(body);
        String cristinPersonAsString = OBJECT_MAPPER.writeValueAsString(cristinPerson);
        JsonNode cristinPersonAsNode = OBJECT_MAPPER.readTree(cristinPersonAsString);

        assertEquals(bodyNode, cristinPersonAsNode);
    }

    @Test
    void nvaModelBuildsCorrectlyWhenTransformingPersonFromCristinToNvaPerson() throws IOException {
        String cristinBody = getBodyFromResource(CRISTIN_GET_PERSON_JSON);
        CristinPerson cristinPerson = fromJson(cristinBody, CristinPerson.class);

        String nvaBody = getBodyFromResource(NVA_API_GET_PERSON_JSON);
        Person expectedNvaPerson = fromJson(nvaBody, Person.class);

        Person actualNvaPerson = cristinPerson.toPerson();

        assertThat(actualNvaPerson, equalTo(expectedNvaPerson));
    }

    private static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }

    private ObjectNode removeIgnoredFields(String body) throws JsonProcessingException {
        ObjectNode nodeTree = (ObjectNode) OBJECT_MAPPER.readTree(body);
        nodeTree.remove("identified_cristin_person");
        nodeTree.remove("cristin_profile_url");
        removeInstitutionFieldFromAffiliations(nodeTree);
        return nodeTree;
    }

    private void removeInstitutionFieldFromAffiliations(ObjectNode nodeTree) {
        ArrayNode affiliations = (ArrayNode) nodeTree.get("affiliations");
        affiliations.forEach(this::removeInstitution);
    }

    private void removeInstitution(JsonNode node) {
        ObjectNode objectNode = (ObjectNode) node;
        objectNode.remove("institution");
    }
}
