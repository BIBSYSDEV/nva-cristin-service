package no.unit.nva.cristin.projects;

import nva.commons.core.Environment;

public class Constants {

    private static final Environment ENVIRONMENT = new Environment();
    private static final String CRISTIN_API_HOST_ENV = "CRISTIN_API_HOST";
    public static final String CRISTIN_API_HOST = ENVIRONMENT.readEnv(CRISTIN_API_HOST_ENV);
}
