package no.unit.nva.cristin.projects;

import static nva.commons.core.attempt.Try.attempt;
import java.net.URI;

public class CommonUtil {

    public static URI buildUri(String... parts) {
        return attempt(() -> new URI(String.join("/", parts))).orElseThrow();
    }
}
