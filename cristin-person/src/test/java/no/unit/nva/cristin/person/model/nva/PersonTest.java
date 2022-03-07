package no.unit.nva.cristin.person.model.nva;

import no.unit.nva.cristin.person.model.cristin.CristinPerson;
import no.unit.nva.cristin.person.model.cristin.CristinPersonPost;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NATIONAL_IDENTITY_NUMBER;
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
        CristinPersonPost cristinPersonPost = dummyPerson().toCristinPersonPost();

        assertEquals(DUMMY_FIRST_NAME, cristinPersonPost.getFirstName());
        assertEquals(DUMMY_LAST_NAME, cristinPersonPost.getSurname());
        assertEquals(DUMMY_FIRST_NAME_PREFERRED, cristinPersonPost.getFirstNamePreferred().orElse(EMPTY_STRING));
        assertEquals(DUMMY_LAST_NAME_PREFERRED, cristinPersonPost.getSurnamePreferred().orElse(EMPTY_STRING));
        assertEquals(DUMMY_NATIONAL_IDENTITY_NUMBER, cristinPersonPost.getNorwegianNationalId());
    }

    private Person dummyPerson() {
        return new Person.Builder().withNames(validNames).withIdentifiers(validIdentifiers).build();
    }
}
