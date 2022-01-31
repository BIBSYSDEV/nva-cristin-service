package no.unit.nva.utils;

import java.net.URI;

public class LanguageMapperExtra {

    public static  String mapMainLanguageToCristin(URI language) {
        switch (language.toString()) {
            case "http://lexvo.org/id/iso639-3/nob" : return "nb";
            case "http://lexvo.org/id/iso639-3/nno" : return "nn";
            case "http://lexvo.org/id/iso639-3/eng" : return "en";
            default:
                throw new IllegalStateException("Unexpected value: " + language);
        }
    }
}
