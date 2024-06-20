package no.unit.nva.cristin.projects.util;

import static java.util.Objects.nonNull;
import static no.unit.nva.language.LanguageMapper.getLanguageByUri;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import no.unit.nva.cristin.projects.model.nva.NvaProject;

public class LanguageUtil {

    public static final String DEFAULT_TITLE_LANGUAGE_KEY = "nb";

    /**
     * Extracts language string in iso 6391 format from a URI.
     **/
    public static String extractLanguageIso6391(URI language) {
        return nonNull(language) ? getLanguageByUri(language).getIso6391Code() : null;
    }

    /**
     * Extracts all titles from a NvaProject and puts them in a hashmap.
     **/
    public static Map<String, String> extractTitles(NvaProject nvaProject) {
        Map<String, String> titles = new ConcurrentHashMap<>();

        if (hasOwnLanguageCode(nvaProject)) {
            addTitleByLanguage(nvaProject, titles);
        } else {
            addTitleByDefaultLanguage(nvaProject.getTitle(), titles);
        }

        nvaProject.getAlternativeTitles().forEach(titles::putAll);

        return titles;
    }

    private static boolean hasOwnLanguageCode(NvaProject nvaProject) {
        return nonNull(nvaProject.getLanguage());
    }

    private static void addTitleByLanguage(NvaProject nvaProject, Map<String, String> titles) {
        var languageKey = getLanguageByUri(nvaProject.getLanguage()).getIso6391Code();
        var languageValue = nvaProject.getTitle();

        titles.put(languageKey, languageValue);
    }

    private static void addTitleByDefaultLanguage(String defaultTitle, Map<String, String> titles) {
        titles.put(DEFAULT_TITLE_LANGUAGE_KEY, defaultTitle);
    }

}
