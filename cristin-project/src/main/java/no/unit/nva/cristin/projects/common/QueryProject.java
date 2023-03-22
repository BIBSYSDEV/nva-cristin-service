package no.unit.nva.cristin.projects.common;

import static no.unit.nva.cristin.model.Constants.CRISTIN_API_URL;
import static no.unit.nva.cristin.model.Constants.EQUAL_OPERATOR;
import static no.unit.nva.cristin.model.Constants.PATTERN_IS_URL;
import static no.unit.nva.cristin.model.Constants.PROJECTS_PATH;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.BIOBANK;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.IGNORE_PATH_PARAMETER_INDEX;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.KEYWORD;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.LANGUAGE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.ORGANIZATION;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PARTICIPANT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PATH_PROJECT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.STATUS;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.CristinQuery;
import no.unit.nva.cristin.model.KeyEncoding;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;
import nva.commons.core.paths.UriWrapper;

public class QueryProject extends CristinQuery<ParameterKeyProject> {
    // https://api.cristin-test.uio.no/v2/doc/index.html#GETprojects

    public static QueryBuilderProject builder() {
        return new QueryBuilderProject();
    }

    /**
     * Creates a URI to Cristin project with specific ID and language.
     *
     * @param id       Project ID to lookup in Cristin
     * @param language what language we want some of the result fields to be in
     * @return an URI to Cristin Projects with ID and language parameters
     */
    public static URI fromIdAndLanguage(String id, String language) {
        return
            UriWrapper.fromUri(CRISTIN_API_URL)
                .addChild(PROJECTS_PATH)
                .addChild(id)
                .addQueryParameters(Map.of(LANGUAGE.getKey(), language))
                .getUri();
    }

    @Override
    protected String toCristinQueryValue(Entry<ParameterKeyProject, String> entry) {
        if (entry.getKey().equals(BIOBANK) || entry.getKey().equals(KEYWORD) || entry.getKey().equals(PARTICIPANT)) {
            final var key = entry.getKey().getKey() + EQUAL_OPERATOR;
            return Arrays.stream(entry.getValue().split(","))
                       .collect(Collectors.joining("&" + key));
        }
        var value = entry.getKey().encoding() == KeyEncoding.ENCODE_DECODE
                        ? encodeUTF(entry.getValue())
                        : entry.getValue();

        if (entry.getKey().equals(STATUS)) {
            return ProjectStatus.valueOf(value).getCristinStatus();
        }
        return entry.getKey().equals(ORGANIZATION) && entry.getValue().matches(PATTERN_IS_URL)
                   ? getUnitIdFromOrganization(value)
                   : value;
    }

    @Override
    protected String getNvaPathItem(int pathSize, Entry<ParameterKeyProject, String> entry) {
        var isProjects = entry.getKey().equals(PATH_PROJECT) && pathSize > 1;
        return isProjects ? entry.getKey().getKey() : entry.getKey().getNvaKey();
    }

    @Override
    protected boolean ignoreNvaPathParameters(Entry<ParameterKeyProject, String> entry) {
        return entry.getKey().ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

    @Override
    protected boolean ignorePathParameters(Entry<ParameterKeyProject, String> f) {
        return f.getKey() != PATH_PROJECT;
    }

    @Override
    protected String[] getCristinPath() {
        return
            containsKey(PATH_PROJECT)
                ? new String[]{PATH_PROJECT.getKey(), getValue(PATH_PROJECT)}
                : new String[]{PATH_PROJECT.getKey()};
    }

}
