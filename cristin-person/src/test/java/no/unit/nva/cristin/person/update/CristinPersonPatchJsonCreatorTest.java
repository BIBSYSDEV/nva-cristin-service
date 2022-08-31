package no.unit.nva.cristin.person.update;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.ErrorMessages.ERROR_MESSAGE_INVALID_PAYLOAD;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_EMPLOYMENTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.RandomPersonData.randomEmployment;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_FIRST_NAME;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_FIRST_NAME_PREFERRED;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_SURNAME;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_SURNAME_PREFERRED;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import nva.commons.apigateway.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;

public class CristinPersonPatchJsonCreatorTest {

    private static final String VALID_ORCID = "1234-1234-1234-1234";
    private static final String SOME_NAME = randomString();
    private static final String SOME_OTHER_NAME = randomString();
    private static final String SOME_PREFERRED_NAME = randomString();

    @Test
    void shouldParseInputJsonIntoCristinJsonWithCorrectMappingOfFields() {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.put(ORCID, VALID_ORCID);
        input.put(FIRST_NAME, SOME_NAME);
        input.put(LAST_NAME, SOME_OTHER_NAME);
        input.put(PREFERRED_FIRST_NAME, SOME_PREFERRED_NAME);
        input.putNull(PREFERRED_LAST_NAME);
        input.put(RESERVED, true);

        ObjectNode result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertEquals(VALID_ORCID, result.get(ORCID).get(ID).asText());
        assertEquals(SOME_NAME, result.get(CRISTIN_FIRST_NAME).asText());
        assertEquals(SOME_OTHER_NAME, result.get(CRISTIN_SURNAME).asText());
        assertEquals(SOME_PREFERRED_NAME, result.get(CRISTIN_FIRST_NAME_PREFERRED).asText());
        assertThat(result.has(CRISTIN_SURNAME_PREFERRED), equalTo(true));
        assertThat(result.get(CRISTIN_SURNAME_PREFERRED).isNull(), equalTo(true));
        assertThat(result.get(RESERVED).asBoolean(), equalTo(true));
    }

    @Test
    void shouldParseOrcidIntoCristinFormatEvenIfNull() {
        ObjectNode input = OBJECT_MAPPER.createObjectNode();
        input.putNull(ORCID);
        ObjectNode result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.get(ORCID).has(ID), equalTo(true));
        assertThat(result.get(ORCID).get(ID).isNull(), equalTo(true));
    }

    @Test
    void shouldNotCreateCristinFieldsIfNotInInput() {
        ObjectNode emptyJson = OBJECT_MAPPER.createObjectNode();
        ObjectNode result = new CristinPersonPatchJsonCreator(emptyJson).create().getOutput();

        assertThat(result.isEmpty(), equalTo(true));
    }

    @Test
    void shouldCreateValidCristinEmploymentOutputWhenInputHasEmploymentData() throws JsonProcessingException {
        var input = OBJECT_MAPPER.createObjectNode();
        var employment = randomEmployment();
        var employmentNode = OBJECT_MAPPER.readTree(employment.toString());
        input.putArray(EMPLOYMENTS).add(employmentNode);
        var output = new CristinPersonPatchJsonCreator(input).create().getOutput();
        var actualEmploymentsNode = output.get(CRISTIN_EMPLOYMENTS);
        var cristinEmploymentsFromNode =
            asList(OBJECT_MAPPER.readValue(actualEmploymentsNode.toString(), CristinPersonEmployment[].class));

        assertThat(cristinEmploymentsFromNode.get(0), equalTo(employment.toCristinEmployment()));
    }

    @Test
    void shouldNotAddEmploymentFieldToOutputIfMissingFromInput() {
        var input = OBJECT_MAPPER.createObjectNode();
        var output = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(output.has(CRISTIN_EMPLOYMENTS), equalTo(false));
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
    void shouldParseRawJsonIntoCristinEmployments() throws BadRequestException, JsonProcessingException {
        var type = "https://api.dev.nva.aws.unit.no/cristin/position#1087";
        var organization = "https://api.dev.nva.aws.unit.no/cristin/organization/20202.0.0.0";
        var startDate = randomInstant().toString();
        var percentage = 80.0;
        var rawJson = "{ \"employments\": [ { \"type\": \"" + type + "\" , \"organization\": \"" + organization + "\""
                      + " , \"startDate\": \"" + startDate + "\" , \"fullTimeEquivalentPercentage\": " + percentage
                      + " } ] }";
        var json = readJsonFromInput(rawJson);
        var output = new CristinPersonPatchJsonCreator(json).create().getOutput();

        var deserialized = Arrays
                               .stream(OBJECT_MAPPER.readValue(output.get(CRISTIN_EMPLOYMENTS).toString(),
                                                               CristinPersonEmployment[].class))
                               .findFirst()
                               .orElseThrow();

        assertThat(deserialized.getPosition().getCode(), equalTo("1087"));
        assertThat(deserialized.getAffiliation().getInstitutionUnit().getCristinUnitId(),
                   equalTo("20202.0.0.0"));
        assertThat(deserialized.getStartDate().toString(), equalTo(startDate));
        assertThat(deserialized.getFtePercentage(), equalTo(percentage));
    }

    private ObjectNode readJsonFromInput(String input) throws BadRequestException {
        return attempt(() -> (ObjectNode) OBJECT_MAPPER.readTree(input))
                   .orElseThrow(fail -> new BadRequestException(ERROR_MESSAGE_INVALID_PAYLOAD));
    }
}
