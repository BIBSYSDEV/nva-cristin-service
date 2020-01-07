package no.unit.nva.cristin.projects;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class FetchCristinProjectsTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    CristinApiClient cristinApiClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


//    @Test
    public void testFetchCristinProjects() {
        CristinApiClient cristinApiClient = new CristinApiClient();
        FetchCristinProjects fetchCristinProjects = new FetchCristinProjects(cristinApiClient);

        String title = "reindeer";
        String language = "nb";
        Map<String, Object> event = new HashMap<>();
        Map<String, String> queryParams = new TreeMap<>();
        queryParams.put("title", title);
        queryParams.put("language", language);
        event.put("queryStringParameters", queryParams);

        SimpleResponse result = fetchCristinProjects.handleRequest(event, null);
        System.out.println(result.body);

        assertNotNull(result);
    }

}
