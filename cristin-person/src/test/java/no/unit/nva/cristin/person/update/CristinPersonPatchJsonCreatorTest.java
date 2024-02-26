package no.unit.nva.cristin.person.update;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_EMPLOYMENTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PERSON_NVI;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NVI;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Path;
import java.util.Collections;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class CristinPersonPatchJsonCreatorTest {

    @Test
    void shouldParseInputJsonIntoCristinJsonWithCorrectMappingOfFieldsAsAuthorizedClient() throws Exception {
        var inputString = IoUtils.stringFromResources(Path.of("nvaApiUpdatePersonAsAdminRequest.json"));
        var input = (ObjectNode) OBJECT_MAPPER.readTree(inputString);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();
        var expectedString = IoUtils.stringFromResources(Path.of("cristinUpdatePersonAsAdminRequest.json"));

        JSONAssert.assertEquals(expectedString, result.toString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldAddAllSupportedFieldsForPersonModifyingThemselves() throws Exception {
        var inputString = IoUtils.stringFromResources(Path.of("nvaApiUpdateOwnPersonRequest.json"));
        var input = (ObjectNode) OBJECT_MAPPER.readTree(inputString);
        var result = new CristinPersonPatchJsonCreator(input).createWithAllowedUserModifiableData().getOutput();
        var expectedString = IoUtils.stringFromResources(Path.of("cristinUpdateOwnPersonRequest.json"));

        JSONAssert.assertEquals(expectedString, result.toString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    void shouldParseOrcidIntoCristinFormatEvenIfNull() {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putNull(ORCID);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.get(ORCID).has(ID), equalTo(true));
        assertThat(result.get(ORCID).get(ID).isNull(), equalTo(true));
    }

    @Test
    void shouldNotCreateCristinFieldsIfNotInInput() {
        var emptyJson = OBJECT_MAPPER.createObjectNode();
        var result = new CristinPersonPatchJsonCreator(emptyJson).create().getOutput();

        assertThat(result.isEmpty(), equalTo(true));
    }

    @Test
    void shouldNotAddEmploymentFieldToOutputWhenEmploymentFieldInInputIsNull() {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putNull(EMPLOYMENTS);
        var output = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(output.has(CRISTIN_EMPLOYMENTS), equalTo(false));
    }

    @Test
    void shouldAddEmploymentFieldContainingEmptyListToOutputWhenEmploymentFieldInInputIsEmptyList() {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putPOJO(EMPLOYMENTS, Collections.emptyList());
        var output = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(output.has(CRISTIN_EMPLOYMENTS), equalTo(true));
        assertThat(output.get(CRISTIN_EMPLOYMENTS).isArray(), equalTo(true));
        assertThat(output.get(CRISTIN_EMPLOYMENTS).isEmpty(), equalTo(true));
    }

    @Test
    void shouldNotAddReservedFieldIfSetToFalse() {
        var input = OBJECT_MAPPER.createObjectNode();
        input.put(RESERVED, false);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(RESERVED), equalTo(false));
    }

    @Test
    void shouldNotAddReservedFieldWhenNotABoolean() {
        var input = OBJECT_MAPPER.createObjectNode();
        input.put(RESERVED, randomString());
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(RESERVED), equalTo(false));
    }

    @Test
    void shouldAllowEmptyListForKeywords() {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putArray(KEYWORDS);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(KEYWORDS), equalTo(true));
        assertThat(result.get(KEYWORDS).isEmpty(), equalTo(true));
    }

    @Test
    void shouldAllowBackgroundSetToNull() throws Exception {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putNull(BACKGROUND);
        PersonPatchValidator.validate(input);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(BACKGROUND), equalTo(true));
        assertThat(result.get(BACKGROUND).isNull(), equalTo(true));
    }

    @Test
    void shouldAllowBackgroundSetToEmptyObject() throws Exception {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putObject(BACKGROUND);
        PersonPatchValidator.validate(input);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(BACKGROUND), equalTo(true));
        assertThat(result.get(BACKGROUND).isObject(), equalTo(true));
    }

    @Test
    void shouldAllowNviDataSetToNull() throws Exception {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putNull(NVI);
        PersonPatchValidator.validate(input);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(PERSON_NVI), equalTo(true));
        assertThat(result.get(PERSON_NVI).isNull(), equalTo(true));
    }

}
