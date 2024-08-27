package no.unit.nva.utils;

import static no.unit.nva.utils.UriUtils.addLanguage;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import java.net.URI;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UriUtilsTest {

    public static final String URI_WITH_INVALID_ENCODING = "https://api.nva.no/?name=t%EF%BF%BDrresen&lang=en%2Cnb%2Cnn";
    public static final String INVALID_ENCODING = "%EF%BF%BD";

    @ParameterizedTest()
    @ValueSource(strings = {"https://api.nva.no/?name=tørresen", "https://api.nva.no/?name=t%C3%B8rresen"})
    void shouldNotBreakEncodingWhenAddingLanguagesToUri(String value) {
        var uri = URI.create(value);
        var uriWithLanguage = addLanguage(uri);

        assertThat(uriWithLanguage.toString(), not(equalTo(URI_WITH_INVALID_ENCODING)));
        assertThat(uriWithLanguage.toString(), not(containsString(INVALID_ENCODING)));
    }

}
