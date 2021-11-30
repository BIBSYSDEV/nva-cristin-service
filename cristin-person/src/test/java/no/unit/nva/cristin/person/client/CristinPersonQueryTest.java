package no.unit.nva.cristin.person.client;

import static no.unit.nva.cristin.model.JsonPropertyNames.NUMBER_OF_RESULTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.PAGE;
import static no.unit.nva.cristin.model.JsonPropertyNames.QUERY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CristinPersonQueryTest {

    private static final String EXAMPLE_IDENTIFIER = "1234";
    private static final String EXPECTED_CRISTIN_URI_WITH_IDENTIFIER =
        "https://api.cristin.no/v2/persons/1234?lang=en,nb,nn";
    private static final String EXPECTED_CRISTIN_URI_WITH_PARAMS =
        "https://api.cristin.no/v2/persons?per_page=5&name=John&page=2&lang=en,nb,nn";
    private static Map<String, String> EXAMPLE_PARAMS =
        Map.of(QUERY, "John", PAGE, "2", NUMBER_OF_RESULTS, "5");

    @Test
    void shouldProduceCorrectCristinUriWhenBuildingUriFromIdentifier() {
        URI actual = CristinPersonQuery.fromId(EXAMPLE_IDENTIFIER);

        assertEquals(EXPECTED_CRISTIN_URI_WITH_IDENTIFIER, actual.toString());
    }

    @Test
    void shouldProduceCorrectCristinUriWhenBuildingUriFromParams() {
        URI actual = new CristinPersonQuery()
            .withName(EXAMPLE_PARAMS.get(QUERY))
            .withFromPage(EXAMPLE_PARAMS.get(PAGE))
            .withItemsPerPage(EXAMPLE_PARAMS.get(NUMBER_OF_RESULTS))
            .toURI();

        assertEquals(EXPECTED_CRISTIN_URI_WITH_PARAMS, actual.toString());
    }
}
