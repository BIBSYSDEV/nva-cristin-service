package no.unit.nva.utils;

import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Map;

import static no.unit.nva.cristin.projects.Constants.BASE_PATH;
import static no.unit.nva.cristin.projects.Constants.DOMAIN_NAME;
import static no.unit.nva.cristin.projects.Constants.HTTPS;

public class UriUtils {

    public static final String PROJECT = "project";
    public static final String INSTITUTION = "institution";

    public static URI createUriFromParams(Map<String, String> parameters, String module) {
        UriWrapper uriWrapper = new UriWrapper(HTTPS, DOMAIN_NAME).addChild(BASE_PATH).addChild(module);
        for (Map.Entry<String, String> e : parameters.entrySet()) {
            uriWrapper = uriWrapper.addQueryParameter(e.getKey(), e.getValue());
        }

        return uriWrapper.getUri();
    }

}
