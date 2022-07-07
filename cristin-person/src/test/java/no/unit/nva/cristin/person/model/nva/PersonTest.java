package no.unit.nva.cristin.person.model.nva;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import no.unit.nva.cristin.model.CristinOrganization;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.affiliations.model.CristinPositionCode;
import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
import static no.unit.nva.cristin.person.model.nva.Person.mapEmploymentsToCristinEmployments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonTest {

    private static final String DUMMY_FIRST_NAME = "Kjell Erik";
    private static final String DUMMY_LAST_NAME = "Olsen-Johnsen";
    private static final String DUMMY_FIRST_NAME_PREFERRED = "Kjell";
    private static final String DUMMY_LAST_NAME_PREFERRED = "Olsen";
    private static final String DUMMY_NATIONAL_IDENTITY_NUMBER = "12345612345";
    private static final Set<TypedValue> validNames =
        Set.of(new TypedValue(CristinPerson.FIRST_NAME, DUMMY_FIRST_NAME),
            new TypedValue(CristinPerson.LAST_NAME, DUMMY_LAST_NAME),
            new TypedValue(CristinPerson.PREFERRED_FIRST_NAME, DUMMY_FIRST_NAME_PREFERRED),
            new TypedValue(CristinPerson.PREFERRED_LAST_NAME, DUMMY_LAST_NAME_PREFERRED));
    private static final Set<TypedValue> validIdentifiers =
        Set.of(new TypedValue(NATIONAL_IDENTITY_NUMBER, DUMMY_NATIONAL_IDENTITY_NUMBER));
    private static final String EMPTY_STRING = "";

    @Test
    void shouldProduceCorrectValuesWhenTransformingPersonToCristinPersonPost() {
        var cristinPersonPost = dummyPerson().toCristinPersonPost();

        assertEquals(DUMMY_FIRST_NAME, cristinPersonPost.getFirstName());
        assertEquals(DUMMY_LAST_NAME, cristinPersonPost.getSurname());
        assertEquals(DUMMY_FIRST_NAME_PREFERRED, cristinPersonPost.getFirstNamePreferred().orElse(EMPTY_STRING));
        assertEquals(DUMMY_LAST_NAME_PREFERRED, cristinPersonPost.getSurnamePreferred().orElse(EMPTY_STRING));
        assertEquals(DUMMY_NATIONAL_IDENTITY_NUMBER, cristinPersonPost.getNorwegianNationalId());
    }

    @Test
    void shouldDeserializePersonWithEmploymentsCorrectly() throws IOException {
        var payload = IoUtils.stringFromResources(Path.of("nvaApiCreatePersonRequest.json"));
        var person = OBJECT_MAPPER.readValue(payload, Person.class);
        var actualEmployments =
            mapEmploymentsToCristinEmployments(person.getEmployments());
        var expectedEmployments = generateExpectedEmploymentsMatchingJson();

        assertThat(actualEmployments, equalTo(expectedEmployments));
    }

    private List<CristinPersonEmployment> generateExpectedEmploymentsMatchingJson() {
        var cristinEmployment = new CristinPersonEmployment();
        var position = new CristinPositionCode();
        position.setCode("1087");
        cristinEmployment.setPosition(position);
        var organization = new CristinOrganization();
        var unit = new CristinUnit();
        unit.setCristinUnitId("20202.0.0.0");
        organization.setInstitutionUnit(unit);
        cristinEmployment.setAffiliation(organization);
        cristinEmployment.setStartDate(Instant.parse("2008-01-01T00:00:00Z"));
        cristinEmployment.setEndDate(Instant.parse("2023-12-31T00:00:00Z"));
        cristinEmployment.setFtePercentage(80.0);

        return List.of(cristinEmployment);
    }

    private Person dummyPerson() {
        return new Person.Builder().withNames(validNames).withIdentifiers(validIdentifiers).build();
    }
}
