package no.unit.nva.cristin.projects;

import java.util.Arrays;
import java.util.Map;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import nva.commons.core.StringUtils;

public class CommonUtils {

    /**
     * Validate content of a CristinProject.
     *
     * @param cristinProject the CristinProject to validate
     * @return true if valid or false if not
     */
    public static boolean hasValidContent(CristinProject cristinProject) {
        return cristinProject != null
            && hasData(cristinProject.cristinProjectId)
            && hasData(cristinProject.title);
    }

    public static boolean hasData(String... strings) {
        return Arrays.stream(strings).noneMatch(StringUtils::isBlank);
    }

    @SuppressWarnings("rawtypes")
    public static boolean hasData(Map... maps) {
        return Arrays.stream(maps).noneMatch(map -> map == null || map.isEmpty());
    }
}
