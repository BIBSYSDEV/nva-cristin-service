package no.unit.nva.cristin.person;

import static no.unit.nva.cristin.model.Constants.HASHTAG;
import static no.unit.nva.cristin.model.Constants.SLASH_DELIMITER;
import static no.unit.nva.testutils.RandomDataGenerator.randomInstant;
import static no.unit.nva.testutils.RandomDataGenerator.randomInteger;
import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import no.unit.nva.cristin.person.model.nva.Employment;

public class RandomPersonData {

    public static final String SOME_UNIT_IDENTIFIER = "185.90.0.0";
    public static final String HEAD_ENGINEER_CODE = HASHTAG + "1087";

    /**
     * Generates a Set of random employments.
     */
    public static Set<Employment> randomEmployments() {
        var employments = new HashSet<Employment>();
        IntStream.range(0, 3).mapToObj(i -> randomEmployment()).forEach(employments::add);
        return employments;
    }

    /**
     * Generates one random employment.
     */
    public static Employment randomEmployment() {
        return new Employment.Builder()
                   .withOrganization(URI.create(randomUri() + SLASH_DELIMITER + SOME_UNIT_IDENTIFIER))
                   .withType(URI.create(randomUri() + HEAD_ENGINEER_CODE))
                   .withStartDate(randomInstant())
                   .withEndDate(randomInstant())
                   .withFullTimeEquivalentPercentage(randomInteger(100).doubleValue())
                   .build();
    }

}
