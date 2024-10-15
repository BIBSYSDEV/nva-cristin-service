package no.unit.nva.cristin.organization.common.client.v20230526;

import static no.unit.nva.cristin.testing.HttpResponseFaker.LINK_EXAMPLE_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import java.net.http.HttpHeaders;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import org.junit.jupiter.api.Test;

class MultiPageProcessorTest {

    public static final String X_TOTAL_COUNT_OVER_PAGE_LIMIT = "1500";
    public static final String X_TOTAL_COUNT_UNDER_PAGE_LIMIT = "500";
    public static final String EMPTY_ARRAY = "[]";

    @Test
    void shouldHaveAdditionalPagesAsTrueWhenTotalCountHeaderIsGreaterThanPaginatedValue() {
        var fakeHeaders = HttpHeaders.of(
            HttpResponseFaker.headerMap(X_TOTAL_COUNT_OVER_PAGE_LIMIT, LINK_EXAMPLE_VALUE),
            HttpResponseFaker.filter()
        );
        var fakeResponse = new HttpResponseFaker(EMPTY_ARRAY, 200, fakeHeaders);
        var multiPageProcessor = new MultiPageProcessor(fakeResponse);

        assertThat(multiPageProcessor.hasAdditionalPages(), equalTo(true));
    }

    @Test
    void shouldHaveAdditionalPagesAsFalseWhenTotalCountHeaderIsLessThanPaginatedValue() {
        var fakeHeaders = HttpHeaders.of(
            HttpResponseFaker.headerMap(X_TOTAL_COUNT_UNDER_PAGE_LIMIT, LINK_EXAMPLE_VALUE),
            HttpResponseFaker.filter()
        );
        var fakeResponse = new HttpResponseFaker(EMPTY_ARRAY, 200, fakeHeaders);
        var multiPageProcessor = new MultiPageProcessor(fakeResponse);

        assertThat(multiPageProcessor.hasAdditionalPages(), equalTo(false));
    }

}
