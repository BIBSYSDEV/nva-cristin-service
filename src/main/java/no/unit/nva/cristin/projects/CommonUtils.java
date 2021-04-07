package no.unit.nva.cristin.projects;

import java.util.Arrays;
import java.util.Map;
import nva.commons.core.StringUtils;

public class CommonUtils {

    public static boolean hasData(String... strings) {
        return Arrays.stream(strings).noneMatch(StringUtils::isBlank);
    }

    @SuppressWarnings("rawtypes")
    public static boolean hasData(Map... maps) {
        return Arrays.stream(maps).noneMatch(map -> map == null || map.isEmpty());
    }
}
