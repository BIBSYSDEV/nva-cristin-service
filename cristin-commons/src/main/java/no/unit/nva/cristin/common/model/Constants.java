package no.unit.nva.cristin.common.model;

import com.google.common.net.MediaType;
import java.util.List;
import nva.commons.apigateway.MediaTypes;

public class Constants {

    public static final String X_TOTAL_COUNT = "x-total-count";
    public static final String PAGE = "page";
    public static final String NUMBER_OF_RESULTS = "results";
    public static final String LINK = "link";
    public static final String REL_NEXT = "rel=\"next\"";
    public static final String REL_PREV = "rel=\"prev\"";
    public static final String QUERY = "query";
    public static final String LANGUAGE = "language";
    public static final String FIRST_PAGE = "1";
    public static final String DEFAULT_NUMBER_OF_RESULTS = "5";
    public static final List<MediaType> DEFAULT_RESPONSE_MEDIA_TYPES = List.of(MediaType.JSON_UTF_8,
        MediaTypes.APPLICATION_JSON_LD);
}
