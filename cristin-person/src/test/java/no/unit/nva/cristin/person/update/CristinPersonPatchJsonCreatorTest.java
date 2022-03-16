package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_FIRST_NAME;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_FIRST_NAME_PREFERRED;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_SURNAME;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_SURNAME_PREFERRED;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class CristinPersonPatchJsonCreatorTest {

    private static final String VALID_ORCID = "1234-1234-1234-1234";
    private static final String SOME_NAME = randomString();
    private static final String SOME_OTHER_NAME = randomString();
    private static final String SOME_PREFERRED_NAME = randomString();

    @Test
    void shouldParseInputJsonIntoCristinJsonWithCorrectMappingOfFields() {
        JSONObject input = new JSONObject();
        input.put(ORCID, VALID_ORCID);
        input.put(FIRST_NAME, SOME_NAME);
        input.put(LAST_NAME, SOME_OTHER_NAME);
        input.put(PREFERRED_FIRST_NAME, SOME_PREFERRED_NAME);
        input.put(PREFERRED_LAST_NAME, JSONObject.NULL);
        input.put(RESERVED, true);

        JSONObject result = new CristinPersonPatchJsonCreator(input).create().getResult();

        assertEquals(VALID_ORCID, result.getJSONObject(ORCID).get(ID));
        assertEquals(SOME_NAME, result.get(CRISTIN_FIRST_NAME));
        assertEquals(SOME_OTHER_NAME, result.get(CRISTIN_SURNAME));
        assertEquals(SOME_PREFERRED_NAME, result.get(CRISTIN_FIRST_NAME_PREFERRED));
        assertThat(result.has(CRISTIN_SURNAME_PREFERRED), equalTo(true));
        assertThat(result.isNull(CRISTIN_SURNAME_PREFERRED), equalTo(true));
        assertEquals(true, result.get(RESERVED));
    }

    @Test
    void shouldParseOrcidIntoCristinFormatEvenIfNull() {
        JSONObject input = new JSONObject();
        input.put(ORCID, JSONObject.NULL);
        JSONObject result = new CristinPersonPatchJsonCreator(input).create().getResult();

        assertThat(result.getJSONObject(ORCID).has(ID), equalTo(true));
        assertThat(result.getJSONObject(ORCID).isNull(ID), equalTo(true));
    }

    @Test
    void shouldNotCreateCristinFieldsIfNotInInput() {
        JSONObject emptyJson = new JSONObject();
        JSONObject result = new CristinPersonPatchJsonCreator(emptyJson).create().getResult();

        assertThat(result.isEmpty(), equalTo(true));
    }
}
