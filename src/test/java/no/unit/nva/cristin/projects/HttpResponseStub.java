package no.unit.nva.cristin.projects;

import static no.unit.nva.cristin.projects.Constants.LINK;
import static no.unit.nva.cristin.projects.Constants.REL_NEXT;
import static no.unit.nva.cristin.projects.Constants.REL_PREV;
import static no.unit.nva.cristin.projects.Constants.X_TOTAL_COUNT;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import javax.net.ssl.SSLSession;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class HttpResponseStub implements HttpResponse<String> {

    public static final String TOTAL_COUNT_EXAMPLE_VALUE = "135";
    public static final String LINK_EXAMPLE_VALUE = String.join(";", REL_PREV, REL_NEXT);
    private String body;
    private int statusCode;

    public HttpResponseStub(String body) {
        this.body = body;
        this.statusCode = 200;
    }

    public HttpResponseStub(String body, int statusCode) {
        this.body = body;
        this.statusCode = statusCode;
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public HttpRequest request() {
        return null;
    }

    @Override
    public Optional<HttpResponse<String>> previousResponse() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return HttpHeaders.of(headerMap(TOTAL_COUNT_EXAMPLE_VALUE, LINK_EXAMPLE_VALUE), filter());
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    @Override
    public URI uri() {
        return null;
    }

    @Override
    public Version version() {
        return null;
    }

    protected static Map<String, List<String>> headerMap(String totalCount, String link) {
        return Map.of(
            X_TOTAL_COUNT, Collections.singletonList(totalCount),
            LINK, Collections.singletonList(link));
    }

    protected static BiPredicate<String, String> filter() {
        return (s, s2) -> true;
    }
}
