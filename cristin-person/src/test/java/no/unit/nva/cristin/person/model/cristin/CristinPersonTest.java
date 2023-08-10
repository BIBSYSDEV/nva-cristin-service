package no.unit.nva.cristin.person.model.cristin;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PERSON_CONTEXT;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class CristinPersonTest {

    private static final String CRISTIN_GET_PERSON_JSON =
        "cristinGetPersonResponse.json";
    private static final String NVA_API_GET_PERSON_JSON =
        "nvaApiGetPersonResponse.json";
    private static final String SOME_NIN = "12345612345";

    @Test
    void shouldBuildCristinModelCorrectlyWhenParsingFromCristinJson() throws IOException {
        String body = getBodyFromResource(CRISTIN_GET_PERSON_JSON);
        CristinPerson cristinPerson = fromJson(body, CristinPerson.class);

        ObjectNode bodyNode = removeIgnoredFields(body);
        String cristinPersonAsString = OBJECT_MAPPER.writeValueAsString(cristinPerson);
        ObjectNode cristinPersonAsNode = (ObjectNode) OBJECT_MAPPER.readTree(cristinPersonAsString);
        removeFieldNotPresentInRawJsonTestData(cristinPersonAsNode);

        assertEquals(bodyNode, cristinPersonAsNode);
    }

    @Test
    void shouldBuildNvaModelCorrectlyWhenConvertingCristinPersonToNvaPerson() throws IOException {
        String cristinBody = getBodyFromResource(CRISTIN_GET_PERSON_JSON);
        CristinPerson cristinPerson = fromJson(cristinBody, CristinPerson.class);

        String nvaBody = getBodyFromResource(NVA_API_GET_PERSON_JSON);
        Person expectedNvaPerson = fromJson(nvaBody, Person.class);
        expectedNvaPerson.setContext(null);

        Person actualNvaPerson = cristinPerson.toPerson();

        assertThat(actualNvaPerson, equalTo(expectedNvaPerson));
    }

    @Test
    void shouldBuildNvaModelCorrectlyWhenConvertingCristinPersonToNvaPersonWithAuthorizedFields() throws IOException {
        var cristinBody = getBodyFromResource(CRISTIN_GET_PERSON_JSON);
        var cristinPerson = fromJson(cristinBody, CristinPerson.class);
        var nin = SOME_NIN;
        cristinPerson.setNorwegianNationalId(nin);
        cristinPerson.setReserved(true);
        cristinPerson.setDetailedAffiliations(Collections.emptyList());

        var nvaBody = getBodyFromResource(NVA_API_GET_PERSON_JSON);
        var expectedNvaPerson = fromJson(nvaBody, Person.class);
        var identifiers = expectedNvaPerson.getIdentifiers();
        identifiers.add(new TypedValue(NATIONAL_IDENTITY_NUMBER, nin));
        expectedNvaPerson.setIdentifiers(identifiers);
        expectedNvaPerson.setReserved(true);
        expectedNvaPerson.setEmployments(Collections.emptySet());
        expectedNvaPerson.setContext(null);

        var actualNvaPerson = cristinPerson.toPersonWithAuthorizedFields();

        assertThat(actualNvaPerson, equalTo(expectedNvaPerson));
    }

    @Test
    void shouldOnlyShowIdentifierAndNotNameWhenDoingOpenQueryAndSomehowAReservedPersonGetsReturned() {
        var cristinPerson = new CristinPerson();
        var identifier = randomInteger(99999).toString();
        cristinPerson.setCristinPersonId(identifier);
        cristinPerson.setFirstName(randomString());
        cristinPerson.setReserved(true);
        var person = cristinPerson.toPerson();

        assertThat(person.getId().toString(), containsString(identifier));
        assertThat(person.getNames().size(), equalTo(0));
    }

    @Test
    void shouldMapAllSupportedFieldsFoundInCristinJsonToCorrectNvaJson() throws Exception {
        var cristinPerson =
            attempt(() -> OBJECT_MAPPER.readValue(fromResources(CRISTIN_GET_PERSON_JSON), CristinPerson.class)).get();
        var nvaPerson = cristinPerson.toPerson();
        nvaPerson.setContext(PERSON_CONTEXT);

        var expected = fromResources(NVA_API_GET_PERSON_JSON);
        var actual = nvaPerson.toString();

        JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
    }

    private String fromResources(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }

    private static <T> T fromJson(String body, Class<T> classOfT) throws IOException {
        return OBJECT_MAPPER.readValue(body, classOfT);
    }

    private String getBodyFromResource(String resource) {
        return IoUtils.stringFromResources(Path.of(resource));
    }

    private ObjectNode removeIgnoredFields(String body) throws JsonProcessingException {
        ObjectNode nodeTree = (ObjectNode) OBJECT_MAPPER.readTree(body);
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

    private void removeFieldNotPresentInRawJsonTestData(ObjectNode node) {
        node.remove("detailed_affiliations");
    }
}
