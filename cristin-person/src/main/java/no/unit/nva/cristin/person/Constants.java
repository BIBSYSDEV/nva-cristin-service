package no.unit.nva.cristin.person;

import nva.commons.core.Environment;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class Constants {

    // TODO: Move to cristin-commons
    public static final String HTTPS = "https";
    // TODO: Can this be shared using cristin-commons?
    private static final Environment ENVIRONMENT = new Environment();
    public static final String BASE_PATH = ENVIRONMENT.readEnv("BASE_PATH");
    public static final String DOMAIN_NAME = ENVIRONMENT.readEnvOpt("DOMAIN_NAME")
        .orElse("api.dev.nva.aws.unit.no");

    public static final String PERSON_CONTEXT = "https://example.org/person-context.json";
}
