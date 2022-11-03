package no.unit.nva.cristin.projects;

import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.ContactInfo;
import no.unit.nva.cristin.projects.model.nva.FundingAmount;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import no.unit.nva.cristin.projects.model.nva.TypedLabel;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomNamesMap;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NvaProjectBuilderTest {

    private static final String API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_WITH_FUNDING_JSON =
        "api_response_one_cristin_project_to_nva_project_with_funding.json";
    private static final String cristinGetProject = stringFromResources(Path.of("cristinGetProjectResponse.json"));
    private static final String CREATED_DATE = "2019-12-31T09:45:17Z";
    private static final String MODIFIED_DATE = "2019-12-31T09:48:20Z";
    private static final String CREATED_BY = "REK";
    public static final String CONTACT_PERSON_NAME = "Helge Testesen";
    public static final String CONTACT_ORGANIZATION = "Agricultural University of Iceland";
    public static final String CONTACT_EMAIL = "helge@test.no";
    public static final String CONTACT_PHONE = "44223355";
    public static final String CURRENCY_CODE_NOK = "NOK";
    public static final double FUNDING_AMOUNT_EXAMPLE = 5660000.0;
    public static final String ENGLISH_LANGUAGE_KEY = "en";
    public static final String APPLIEDRESEARCH_TYPE = "APPLIEDRESEARCH";
    public static final String APPLIEDRESEARCH_LABEL = "Applied Research";

    @Test
    void shouldReturnNvaProjectWhenCallingNvaProjectBuilderMethodWithValidCristinProject() throws Exception {
        String expected = stringFromResources(
                Path.of(API_RESPONSE_ONE_CRISTIN_PROJECT_TO_NVA_PROJECT_WITH_FUNDING_JSON));
        CristinProject cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        NvaProject nvaProject = new NvaProjectBuilder(cristinProject).build();
        nvaProject.setContext(PROJECT_LOOKUP_CONTEXT_URL);
        String actual = attempt(() -> OBJECT_MAPPER.writeValueAsString(nvaProject)).get();

        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }

    @Test
    void shouldMapAllSupportedFieldsFoundInCristinJson() {
        var cristinProject =
            attempt(() -> OBJECT_MAPPER.readValue(cristinGetProject, CristinProject.class)).get();
        var nvaProject = new NvaProjectBuilder(cristinProject).build();

        assertThat(nvaProject.getPublished(), equalTo(true));
        assertThat(nvaProject.getPublishable(), equalTo(true));
        assertThat(nvaProject.getCreated().getSourceShortName(), equalTo(CREATED_BY));
        assertThat(nvaProject.getCreated().getDate().toString(), equalTo(CREATED_DATE));
        assertThat(nvaProject.getLastModified().getDate().toString(), equalTo(MODIFIED_DATE));
        assertThat(nvaProject.getContactInfo().getContactPerson(), equalTo(CONTACT_PERSON_NAME));
        assertThat(nvaProject.getContactInfo().getOrganization(), equalTo(CONTACT_ORGANIZATION));
        assertThat(nvaProject.getContactInfo().getEmail(), equalTo(CONTACT_EMAIL));
        assertThat(nvaProject.getContactInfo().getPhone(), equalTo(CONTACT_PHONE));
        assertThat(nvaProject.getFundingAmount().getCurrency(), equalTo(CURRENCY_CODE_NOK));
        assertThat(nvaProject.getFundingAmount().getValue(), equalTo(FUNDING_AMOUNT_EXAMPLE));
        assertThat(nvaProject.getMethod().get(ENGLISH_LANGUAGE_KEY), not(emptyString()));
        assertThat(nvaProject.getEquipment().get(ENGLISH_LANGUAGE_KEY), not(emptyString()));
        assertThat(nvaProject.getProjectCategories().get(0).getType(), equalTo(APPLIEDRESEARCH_TYPE));
        assertThat(nvaProject.getProjectCategories().get(0).getLabels().get(ENGLISH_LANGUAGE_KEY),
                   equalTo(APPLIEDRESEARCH_LABEL));
    }

    @Test
    void shouldSerializeAndDeserializeContactInfoIntoSameObject() {
        var contactInfo = new ContactInfo(randomString(), randomString(), CONTACT_EMAIL, randomString());
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(contactInfo)).orElseThrow();
        var deserialized =
            attempt(() -> OBJECT_MAPPER.readValue(serialized, ContactInfo.class)).orElseThrow();

        assertThat(deserialized, equalTo(contactInfo));
    }

    @Test
    void shouldSerializeAndDeserializeFundingAmountIntoSameObject() {
        var fundingAmount = new FundingAmount(randomString(), randomInteger().doubleValue());
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(fundingAmount)).orElseThrow();
        var deserialized =
            attempt(() -> OBJECT_MAPPER.readValue(serialized, FundingAmount.class)).orElseThrow();

        assertThat(deserialized, equalTo(fundingAmount));
    }

    @Test
    void shouldSerializeAndDeserializeTypedLabelIntoSameObject() {
        var typedLabel = new TypedLabel(randomString(), randomNamesMap());
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(typedLabel)).orElseThrow();
        var deserialized =
            attempt(() -> OBJECT_MAPPER.readValue(serialized, TypedLabel.class)).orElseThrow();

        assertThat(deserialized, equalTo(typedLabel));
    }

}
