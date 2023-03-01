package no.unit.nva.cristin.projects.model.nva;

import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.Constants.PROJECT_LOOKUP_CONTEXT_URL;
import static nva.commons.core.attempt.Try.attempt;
import static nva.commons.core.ioutils.IoUtils.stringFromResources;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NvaProjectCristinQueryBuilderTest {

    private static final String API_RESPONSE_ONE_NVA_PROJECT_JSON =
        "nvaApiGetResponseOneNvaProject.json";
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
    public static final String KEYWORD_CODE = "4837";
    public static final String KEYWORD_LABEL = "Supply Chain";
    public static final String EXTERNAL_SOURCE_NAME = "REK";
    public static final String EXTERNAL_SOURCE_IDENTIFIER = "2016/2000";
    public static final String RELATED_PROJECT_URI = "https://api.dev.nva.aws.unit.no/cristin/project/483302";
    public static final String ORGANIZATION_IDENTIFIER = "https://api.dev.nva.aws.unit.no/cristin/organization/1234.0"
                                                         + ".0.0";
    public static final String MEDICAL_DEPARTMENT = "Medical Department";

    @Test
    void shouldReturnNvaProjectWhenCallingNvaProjectBuilderMethodWithValidCristinProject() throws Exception {
        String expected = stringFromResources(
                Path.of(API_RESPONSE_ONE_NVA_PROJECT_JSON));
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
        assertThat(nvaProject.getProjectCategories().get(0).getLabel().get(ENGLISH_LANGUAGE_KEY),
                   equalTo(APPLIEDRESEARCH_LABEL));
        assertThat(nvaProject.getKeywords().get(0).getType(), equalTo(KEYWORD_CODE));
        assertThat(nvaProject.getKeywords().get(0).getLabel().get(ENGLISH_LANGUAGE_KEY), equalTo(KEYWORD_LABEL));
        assertThat(nvaProject.getExternalSources().get(0).getIdentifier(), equalTo(EXTERNAL_SOURCE_IDENTIFIER));
        assertThat(nvaProject.getExternalSources().get(0).getName(), equalTo(EXTERNAL_SOURCE_NAME));
        assertThat(nvaProject.getRelatedProjects().get(0).toString(), equalTo(RELATED_PROJECT_URI));
        assertThat(nvaProject.getInstitutionsResponsibleForResearch().get(0).getId().toString(),
                   equalTo(ORGANIZATION_IDENTIFIER));
        assertThat(nvaProject.getInstitutionsResponsibleForResearch().get(0).getName().get(ENGLISH_LANGUAGE_KEY),
                   equalTo(MEDICAL_DEPARTMENT));
        assertThat(nvaProject.getContributors().get(0).getIdentity().getEmail(), notNullValue());
    }

    @Test
    void shouldSerializeAndDeserializeNvaProjectIntoSameObject() {
        var nvaJson =
            stringFromResources(Path.of(API_RESPONSE_ONE_NVA_PROJECT_JSON));
        var nvaProject =
            attempt(() -> OBJECT_MAPPER.readValue(nvaJson, NvaProject.class)).get();
        var serialized = attempt(() -> OBJECT_MAPPER.writeValueAsString(nvaProject)).orElseThrow();
        var deserialized = attempt(() -> OBJECT_MAPPER.readValue(serialized, NvaProject.class)).get();

        assertThat(deserialized, equalTo(nvaProject));
    }

}
