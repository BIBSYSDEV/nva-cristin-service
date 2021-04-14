package no.unit.nva.cristin.projects;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import nva.commons.core.JacocoGenerated;

@JacocoGenerated
public class HttpResponseStub implements HttpResponse<InputStream> {

    private InputStream inputStream;
    private int statusCode;

    public HttpResponseStub(InputStream inputStream) {
        this.inputStream = inputStream;
        this.statusCode = 200;
    }

    public HttpResponseStub(InputStream inputStream, int statusCode) {
        this.inputStream = inputStream;
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
    public Optional<HttpResponse<InputStream>> previousResponse() {
        return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
        return null;
    }

    @Override
    public InputStream body() {
        return inputStream;
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
