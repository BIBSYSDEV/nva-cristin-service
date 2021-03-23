package no.unit.nva.cristin.projects;

import static org.mockito.Mockito.mock;
import com.amazonaws.services.lambda.runtime.Context;
import java.io.ByteArrayOutputStream;
import nva.commons.core.Environment;
import org.junit.jupiter.api.BeforeEach;

public class FetchOneCristinProjectTest {

    private CristinApiClient cristinApiClientStub;
    private Environment environment = new Environment();
    private Context context;
    private ByteArrayOutputStream output;
    private FetchOneCristinProject handler;

    @BeforeEach
    void setUp() {
        cristinApiClientStub = new CristinApiClientStub();
        context = mock(Context.class);
        output = new ByteArrayOutputStream();
        handler = new FetchOneCristinProject(cristinApiClientStub, environment);
    }
}
