package no.unit.nva.cristin.person.institution.fetch;

import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.Tag;

@Tag("karateTest")
public class PersonInstitutionInfoFetchKarateRunner {

    @Karate.Test
    Karate runKarateFeatures() {
        return Karate.run().relativeTo(getClass());
    }
}
