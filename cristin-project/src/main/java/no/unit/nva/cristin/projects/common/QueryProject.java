package no.unit.nva.cristin.projects.common;

import static no.unit.nva.cristin.model.Constants.PATTERN_IS_URL;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.BIOBANK;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.CATEGORY;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.FUNDING_SOURCE;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.IGNORE_PATH_PARAMETER_INDEX;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.KEYWORD;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.ORGANIZATION;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PARTICIPANT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.PATH_PROJECT;
import static no.unit.nva.cristin.projects.common.ParameterKeyProject.STATUS;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import no.unit.nva.cristin.model.CristinQuery;
import no.unit.nva.cristin.model.KeyEncoding;
import no.unit.nva.cristin.projects.model.nva.ProjectStatus;

/**
 *   Parameter definistions  <a href="https://api.cristin-test.uio.no/v2/doc/index.html#GETprojects">...</a>.
 */
public class QueryProject extends CristinQuery<ParameterKeyProject> {

    public static QueryBuilderProject builder() {
        return new QueryBuilderProject();
    }

    @Override
    protected String toCristinQueryValue(Entry<ParameterKeyProject, String> entry) {
        var value = entry.getKey().encoding() == KeyEncoding.ENCODE_DECODE
                        ? encodeUTF(entry.getValue())
                        : entry.getValue();

        if (entry.getKey().equals(STATUS)) {
            return ProjectStatus.valueOf(value).getCristinStatus();
        }

        if (entry.getKey().equals(ORGANIZATION) && entry.getValue().matches(PATTERN_IS_URL)) {
            return getUnitIdFromOrganization(value);
        }

        return value;
    }

    @Override
    protected Collection<String> multiValuedCristinParams() {
        return List.of(BIOBANK.getKey(),
                       KEYWORD.getKey(),
                       PARTICIPANT.getKey(),
                       CATEGORY.getKey(),
                       FUNDING_SOURCE.getKey());
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
