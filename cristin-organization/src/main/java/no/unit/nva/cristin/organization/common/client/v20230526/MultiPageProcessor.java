package no.unit.nva.cristin.organization.common.client.v20230526;

import static no.unit.nva.cristin.model.Constants.X_TOTAL_COUNT;
import java.net.http.HttpResponse;
import java.util.Optional;

public class MultiPageProcessor {

    public static final String FALLBACK_TOTAL_COUNT_MISSING = "0";
    public static final int MAX_PAGE_SIZE = 1000;

    private final HttpResponse<String> httpResponse;

    public MultiPageProcessor(HttpResponse<String> httpResponse) {
        this.httpResponse = httpResponse;
    }

    public boolean hasAdditionalPages() {
        return numeric(totalCountHeader().orElse(FALLBACK_TOTAL_COUNT_MISSING)) > MAX_PAGE_SIZE;
    }

    private Optional<String> totalCountHeader() {
        return httpResponse.headers().firstValue(X_TOTAL_COUNT);
    }

    private Integer numeric(String value) {
        return Integer.parseInt(value);
    }

}
