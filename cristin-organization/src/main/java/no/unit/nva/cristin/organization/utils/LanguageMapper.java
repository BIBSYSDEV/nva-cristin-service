package no.unit.nva.cristin.organization.utils;

import no.unit.nva.cristin.organization.exception.UnknownLanguageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;

public class LanguageMapper {

    public static final String LOG_LANGUAGE_MAPPING_TEMPLATE = "Attempting to find language \"%s\"";

    private static final Logger logger = LoggerFactory.getLogger(LanguageMapper.class);

    /**
     * Returns a {@link Language} object for a valid language code.
     * See valid language codes in {@link Language} class.
     *
     * @param languageCode a valid language code.
     * @return a {@link Language} instance
     * @throws UnknownLanguageException when the language code is not a valid code.
     */
    public Language getLanguage(String languageCode) throws UnknownLanguageException {
        if (isNull(languageCode) || languageCode.isBlank()) {
            logger.warn("LanguageCode could not be found");
            return Language.DEFAULT_LANGUAGE;
        }
        logger.info(String.format(LOG_LANGUAGE_MAPPING_TEMPLATE, languageCode));
        return getLanguage(languageCode);
    }
}
