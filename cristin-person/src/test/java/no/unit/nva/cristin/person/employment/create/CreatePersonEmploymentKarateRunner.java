package no.unit.nva.cristin.person.employment.create;

import com.intuit.karate.junit5.Karate;
import no.unit.nva.cristin.person.employment.ClearCristinTestPersonEmployment;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("karateTest")
public class CreatePersonEmploymentKarateRunner {

    public static final String TEST_PERSON_IDENTIFIER = "CRISTIN_EMPLOYMENT_TEST_PERSON_IDENTIFIER";
    public static final String ERROR_READING_ENVIRONMENT_VARIABLE_MESSAGE =
            "Error reading environment variable " + TEST_PERSON_IDENTIFIER;
    public static final String APIGATEWAY_EXCEPTION_DELETING_EMPLOYMENT =
            "ApiGatewayException deleting employment for person";

    @BeforeAll
    static void clearTestUserEmployments() {
        try {
            String cristinTestUserId = new Environment().readEnv(TEST_PERSON_IDENTIFIER);
            ClearCristinTestPersonEmployment.clearEmployment(cristinTestUserId);
        } catch (ApiGatewayException e) {
            fail(APIGATEWAY_EXCEPTION_DELETING_EMPLOYMENT, e);
        } catch (IllegalStateException e) {
            fail(ERROR_READING_ENVIRONMENT_VARIABLE_MESSAGE);
        }
    }

    @Karate.Test
    Karate runKarateFeatures() {
        return Karate.run().relativeTo(getClass());
    }
}
