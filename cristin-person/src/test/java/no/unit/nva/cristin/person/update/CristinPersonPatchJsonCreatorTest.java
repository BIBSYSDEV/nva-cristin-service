package no.unit.nva.cristin.person.update;

import static java.util.Arrays.asList;
import static no.unit.nva.cristin.common.Utils.readJsonFromInput;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_EMPLOYMENTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_UNIT_ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.UNIT;
import static no.unit.nva.cristin.person.RandomPersonData.randomEmployment;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PERSON_NVI;
import static no.unit.nva.cristin.person.model.cristin.CristinPersonNvi.VERIFIED_AT;
import static no.unit.nva.cristin.person.model.cristin.CristinPersonNvi.VERIFIED_BY;
import static no.unit.nva.cristin.person.model.cristin.CristinPersonSummary.CRISTIN_PERSON_ID;
import static no.unit.nva.cristin.person.model.nva.ContactDetails.EMAIL;
import static no.unit.nva.cristin.person.model.nva.ContactDetails.TELEPHONE;
import static no.unit.nva.cristin.person.model.nva.ContactDetails.WEB_PAGE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CONTACT_DETAILS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CRISTIN_TELEPHONE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CRISTIN_WEB_PAGE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NVI;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_FIRST_NAME;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_FIRST_NAME_PREFERRED;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_SURNAME;
import static no.unit.nva.cristin.person.update.CristinPersonPatchJsonCreator.CRISTIN_SURNAME_PREFERRED;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.Collections;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.paths.UriWrapper;
import org.junit.jupiter.api.Test;

public class CristinPersonPatchJsonCreatorTest {

    private static final String VALID_ORCID = "1234-1234-1234-1234";
    private static final String SOME_NAME = randomString();
    private static final String SOME_OTHER_NAME = randomString();
    private static final String SOME_PREFERRED_NAME = randomString();
    public static final String END_OF_LITERAL = "\" , ";
    public static final String ENGLISH_LANG = "en";
    public static final String NORWEGIAN_LANG = "no";
    public static final String NORWEGIAN_LANG_CONTENT = "Norsk";
    public static final String SOME_NUMBER = "11223344";
    public static final String SOME_EMAIL = "hello@example.org";
    public static final String SOME_WEBPAGE = "www.example.org";

    @Test
    void shouldParseInputJsonIntoCristinJsonWithCorrectMappingOfFields() {
        var input = OBJECT_MAPPER.createObjectNode();
        input.put(ORCID, VALID_ORCID);
        input.put(FIRST_NAME, SOME_NAME);
        input.put(LAST_NAME, SOME_OTHER_NAME);
        input.put(PREFERRED_FIRST_NAME, SOME_PREFERRED_NAME);
        input.putNull(PREFERRED_LAST_NAME);
        input.put(RESERVED, true);

        var contactDetails = OBJECT_MAPPER.createObjectNode();
        contactDetails.put(TELEPHONE, SOME_NUMBER);
        contactDetails.put(EMAIL, SOME_EMAIL);
        contactDetails.put(WEB_PAGE, SOME_WEBPAGE);

        input.set(CONTACT_DETAILS, contactDetails);

        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertEquals(VALID_ORCID, result.get(ORCID).get(ID).asText());
        assertEquals(SOME_NAME, result.get(CRISTIN_FIRST_NAME).asText());
        assertEquals(SOME_OTHER_NAME, result.get(CRISTIN_SURNAME).asText());
        assertEquals(SOME_PREFERRED_NAME, result.get(CRISTIN_FIRST_NAME_PREFERRED).asText());
        assertThat(result.has(CRISTIN_SURNAME_PREFERRED), equalTo(true));
        assertThat(result.get(CRISTIN_SURNAME_PREFERRED).isNull(), equalTo(true));
        assertThat(result.get(RESERVED).asBoolean(), equalTo(true));
        assertThat(result.get(CRISTIN_TELEPHONE).asText(), equalTo(SOME_NUMBER));
        assertThat(result.get(EMAIL).asText(), equalTo(SOME_EMAIL));
        assertThat(result.get(CRISTIN_WEB_PAGE).asText(), equalTo(SOME_WEBPAGE));
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
        var employment = randomEmployment();
        var cristinEmployment = employment.toCristinEmployment();
        var rawJson =
            "{ \"employments\": [ { "
            + "\"type\": \"" + employment.getType() + END_OF_LITERAL
            + "\"organization\": \"" + employment.getOrganization() + END_OF_LITERAL
            + "\"startDate\": \"" + employment.getStartDate().toString() + END_OF_LITERAL
            + "\"endDate\": \"" + employment.getEndDate().toString() + END_OF_LITERAL
            + "\"fullTimeEquivalentPercentage\": \"" + employment.getFullTimeEquivalentPercentage() + "\""
            + " } ] }";
        var json = readJsonFromInput(rawJson);
        var output = new CristinPersonPatchJsonCreator(json).create().getOutput();
        var deserialized = Arrays
                               .stream(OBJECT_MAPPER.readValue(output.get(CRISTIN_EMPLOYMENTS).toString(),
                                                               CristinPersonEmployment[].class))
                               .findFirst()
                               .orElseThrow();

        assertThat(deserialized, equalTo(cristinEmployment));
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
    void shouldAddKeywordsFieldToOutputWhenInputHasKeywords() throws Exception {
        var input = OBJECT_MAPPER.createObjectNode();
        var keyword = new TypedValue(randomString(), randomString());
        input.putArray(KEYWORDS).add(OBJECT_MAPPER.readTree(keyword.toString()));
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(KEYWORDS), equalTo(true));

        var keywordFromJson =
            OBJECT_MAPPER.readValue(result.get(KEYWORDS).toString(), CristinTypedLabel[].class);

        assertThat(keywordFromJson[0].getCode(), equalTo(keyword.getType()));
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
    void shouldAllowBackgroundWithLanguageContentBothNullAndWithValue() throws Exception {
        var input = OBJECT_MAPPER.createObjectNode();
        input.putObject(BACKGROUND).putNull(ENGLISH_LANG).put(NORWEGIAN_LANG, NORWEGIAN_LANG_CONTENT);
        PersonPatchValidator.validate(input);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.has(BACKGROUND), equalTo(true));
        assertThat(result.get(BACKGROUND).isObject(), equalTo(true));
        assertThat(result.get(BACKGROUND).get(ENGLISH_LANG).isNull(), equalTo(true));
        assertThat(result.get(BACKGROUND).get(NORWEGIAN_LANG).asText(), equalTo(NORWEGIAN_LANG_CONTENT));
    }

    @Test
    void shouldAllowNviDataSetToValidValues() throws Exception {
        var cristinUnitIdHavingVerified = "20754.0.0.0";
        var cristinPersonIdHavingVerified = "1234";
        var unitIdString = getIdUriString(cristinUnitIdHavingVerified);
        var personIdString = getIdUriString(cristinPersonIdHavingVerified);

        var input = OBJECT_MAPPER.createObjectNode();
        var nvi = OBJECT_MAPPER.createObjectNode();
        var verifiedBy = OBJECT_MAPPER.createObjectNode().put(ID, personIdString);
        var verifiedAt = OBJECT_MAPPER.createObjectNode().put(ID, unitIdString);
        nvi.set(PersonNvi.VERIFIED_BY, verifiedBy);
        nvi.set(PersonNvi.VERIFIED_AT, verifiedAt);
        input.set(NVI, nvi);

        PersonPatchValidator.validate(input);
        var result = new CristinPersonPatchJsonCreator(input).create().getOutput();

        assertThat(result.get(PERSON_NVI)
                       .get(VERIFIED_AT)
                       .get(UNIT)
                       .get(CRISTIN_UNIT_ID)
                       .asText(),
                   equalTo(cristinUnitIdHavingVerified));
        assertThat(result.get(PERSON_NVI)
                       .get(VERIFIED_BY)
                       .get(CRISTIN_PERSON_ID)
                       .asText(),
                   equalTo(cristinPersonIdHavingVerified));
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

    @Test
    void shouldAddAllSupportedFieldsForPersonModifyingThemselves() throws Exception {
        var input = OBJECT_MAPPER.createObjectNode();
        input.put(ORCID, VALID_ORCID);
        input.put(PREFERRED_FIRST_NAME, SOME_PREFERRED_NAME);
        input.putNull(PREFERRED_LAST_NAME);
        var keyword = new TypedValue(randomString(), randomString());
        input.putArray(KEYWORDS).add(OBJECT_MAPPER.readTree(keyword.toString()));
        input.putObject(BACKGROUND).putNull(ENGLISH_LANG).put(NORWEGIAN_LANG, NORWEGIAN_LANG_CONTENT);

        var result = new CristinPersonPatchJsonCreator(input).createWithAllowedUserModifiableData().getOutput();

        assertEquals(VALID_ORCID, result.get(ORCID).get(ID).asText());
        assertEquals(SOME_PREFERRED_NAME, result.get(CRISTIN_FIRST_NAME_PREFERRED).asText());
        assertThat(result.has(CRISTIN_SURNAME_PREFERRED), equalTo(true));
        assertThat(result.get(CRISTIN_SURNAME_PREFERRED).isNull(), equalTo(true));
        assertThat(result.has(KEYWORDS), equalTo(true));
        var keywordFromJson =
            OBJECT_MAPPER.readValue(result.get(KEYWORDS).toString(), CristinTypedLabel[].class);
        assertThat(keywordFromJson[0].getCode(), equalTo(keyword.getType()));
        assertThat(result.has(BACKGROUND), equalTo(true));
        assertThat(result.get(BACKGROUND).isObject(), equalTo(true));
        assertThat(result.get(BACKGROUND).get(ENGLISH_LANG).isNull(), equalTo(true));
        assertThat(result.get(BACKGROUND).get(NORWEGIAN_LANG).asText(), equalTo(NORWEGIAN_LANG_CONTENT));
    }

    private static String getIdUriString(String identifier) {
        return UriWrapper.fromUri(randomUri()).addChild(identifier).toString();
    }

}
