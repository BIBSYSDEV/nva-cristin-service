package no.unit.nva.cristin.testing;

import nva.commons.core.JacocoGenerated;

import javax.net.ssl.SSLSession;
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

import static no.unit.nva.cristin.model.Constants.LINK;
import static no.unit.nva.cristin.model.Constants.REL_NEXT;
import static no.unit.nva.cristin.model.Constants.REL_PREV;
import static no.unit.nva.cristin.model.Constants.X_TOTAL_COUNT;

@JacocoGenerated
public class HttpResponseFaker implements HttpResponse<String> {

    public static final String TOTAL_COUNT_EXAMPLE_VALUE = "135";
    public static final String LINK_EXAMPLE_VALUE = String.join(";", REL_PREV, REL_NEXT);
    private final transient String bodyString;
    private final transient int status;
    private final transient HttpHeaders httpHeaders;

    public HttpResponseFaker(String bodyString) {
        this(bodyString, 200);
    }

    public HttpResponseFaker(String bodyString, int status) {
        this(bodyString, status, defaultHeaders());
    }

    /**
     * Main constructor for a stub of HttpResponse.
     *
     * @param bodyString  Body content of HttpResponse
     * @param status      Http status code of HttpResponse
     * @param httpHeaders HttpHeaders used in the HttpResponse
     */
    public HttpResponseFaker(String bodyString, int status, HttpHeaders httpHeaders) {
        this.bodyString = bodyString;
        this.status = status;
        this.httpHeaders = httpHeaders;
    }

    /**
     * Create map of headers.
     * @param totalCount count header
     * @param link link header
     * @return map of headers
     */
    public static Map<String, List<String>> headerMap(String totalCount, String link) {
        return Map.of(
            X_TOTAL_COUNT, Collections.singletonList(totalCount),
            LINK, Collections.singletonList(link));
    }

    public static BiPredicate<String, String> filter() {
        return (s, s2) -> true;
    }

    @Override
    public int statusCode() {
        return status;
    }

    @Override
    public HttpHeaders headers() {
        return httpHeaders;
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
    public String body() {
        return bodyString;
    }

    private static HttpHeaders defaultHeaders() {
        return HttpHeaders.of(headerMap(TOTAL_COUNT_EXAMPLE_VALUE, LINK_EXAMPLE_VALUE), filter());
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

}
