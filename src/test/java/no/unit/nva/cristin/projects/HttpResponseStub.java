package no.unit.nva.cristin.projects;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class HttpResponseStub implements HttpResponse<String> {

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
        return null;
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
}
