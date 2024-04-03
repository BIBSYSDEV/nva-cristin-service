package no.unit.nva.cristin.intermediate.storage;

import static io.vavr.control.Try.of;
import static io.vavr.control.Try.ofSupplier;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import no.unit.nva.commons.json.JsonUtils;
import nva.commons.core.Environment;
import nva.commons.core.useragent.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for retrieving all Norwegian cristin units where you want to do repeated lookups and keep a large
 * cache like migrations and other bulk operations.
 */
public class CristinUnitsUtil {

    private static final Logger logger = LoggerFactory.getLogger(CristinUnitsUtil.class);
    private static final String API_ARGUMENTS = "/units?per_page=1000&country=NO&page=";
    public static final String APPLICATION_JSON = "application/json";
    public static final String ACCEPT = "Accept";
    public static final String USER_AGENT = "User-Agent";
    public static final String API_HOST = "CRISTIN_API_URL";
    public static final String SIKT_EMIL = "support@sikt.no";
    public static final URI GITHUB_REPO = URI.create("https://github.com/BIBSYSDEV/nva-publication-api");
    private final URI apiUri;
    private final HttpClient httpClient;
    private final Environment environment;
    private final Class<?> caller;
    private final String cristinBotFilterBypassHeaderName;
    private final String cristinBotFilterBypassHeaderValue;

    public CristinUnitsUtil(HttpClient httpClient, URI apiUri, Environment environment, Class<?> caller,
                            String cristinBotFilterBypassHeaderName, String cristinBotFilterBypassHeaderValue) {
        this.httpClient = httpClient;
        this.apiUri = apiUri;
        this.environment = environment;
        this.caller = caller;
        this.cristinBotFilterBypassHeaderName = cristinBotFilterBypassHeaderName;
        this.cristinBotFilterBypassHeaderValue = cristinBotFilterBypassHeaderValue;
    }

    public String getAllData() {
        int pageNum = 1;
        ArrayNode mergedArray = JsonUtils.dtoObjectMapper.createArrayNode();

        while (true) {
            var units = getApiData(pageNum);
            if (units.isEmpty()) {
                break;
            } else {
                mergedArray.addAll(units);
                pageNum++;
            }
        }

        return mergedArray.toString();
    }

    private ArrayNode getApiData(int pageNum) {
        try {
            var response = fetchResponseWithRetry(new URI(apiUri + API_ARGUMENTS + pageNum));
            return attempt(() -> (ArrayNode) JsonUtils.dtoObjectMapper.readTree(response)).orElseThrow();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String fetchResponseWithRetry(URI requestUri) {
        var retryRegistry = RetryRegistry.of(RetryConfig.custom().maxAttempts(5).intervalFunction(
            IntervalFunction.ofExponentialRandomBackoff()).build());
        var retryWithDefaultConfig = retryRegistry.retry("executeRequest");
        Supplier<String> supplier = () -> executeRequest(requestUri);

        return ofSupplier(Retry.decorateSupplier(retryWithDefaultConfig, supplier)).get();
    }

    private String executeRequest(URI requestUri) {
        logger.info("Fetching data from {}", requestUri);
        return of(() -> attempt(() -> httpClient.send(buildHttpRequest(requestUri),
                                                      BodyHandlers.ofString(StandardCharsets.UTF_8)))
                            .map(HttpResponse::body)
                            .toOptional().orElseThrow()).get();
    }

    private HttpRequest buildHttpRequest(URI requestUri) {
        return HttpRequest.newBuilder()
                   .uri(requestUri)
                   .headers(ACCEPT, APPLICATION_JSON,
                            cristinBotFilterBypassHeaderName,
                            cristinBotFilterBypassHeaderValue,
                            USER_AGENT, getUserAgent())
                   .GET()
                   .build();
    }

    private String getUserAgent() {
        return UserAgent.newBuilder().client(caller)
                   .environment(this.environment.readEnv(API_HOST))
                   .repository(GITHUB_REPO)
                   .email(SIKT_EMIL)
                   .version("1.0")
                   .build().toString();
    }
}
