package no.unit.nva.cristin.projects;

import java.nio.file.Path;
import java.util.stream.Stream;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class TestPairProvider implements ArgumentsProvider {

    public static final String CRISTIN_QUERY_PROJECTS_RESPONSE = "cristinQueryProjectsResponse.json";
    public static final String CRISTIN_GET_PROJECT_RESPONSE = "cristinGetProjectResponse.json";
    public static final String API_QUERY_RESPONSE_JSON =
        IoUtils.stringFromResources(Path.of("api_query_response.json"));

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(
                Arguments.of(CRISTIN_QUERY_PROJECTS_RESPONSE, CRISTIN_GET_PROJECT_RESPONSE, API_QUERY_RESPONSE_JSON)
        );
    }
}
