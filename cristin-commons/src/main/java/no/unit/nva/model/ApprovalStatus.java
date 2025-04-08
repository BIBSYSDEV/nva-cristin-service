package no.unit.nva.model;

import static java.util.Arrays.stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Collectors;
import nva.commons.core.SingletonCollector;

public enum ApprovalStatus {

    NOT_APPLIED("NotApplied"),
    APPLIED("Applied"),
    APPROVED("Approved"),
    DECLINED("Declined"),
    REJECTED("Rejected");

    public static final String ERROR_MESSAGE_TEMPLATE = "Supplied ApprovalStatus is not valid expected one of: %s";
    public static final String DELIMITER = ", ";

    private final String statusValue;

    ApprovalStatus(String statusValue) {
        this.statusValue = statusValue;
    }

    @JsonValue
    public String getStatusValue() {
        return statusValue;
    }

    /**
     * Lookup ApprovalStatus by json. If value not supported, throws exception.
     */
    @JsonCreator
    public static ApprovalStatus fromJson(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getStatusValue().equalsIgnoreCase(value))
                   .collect(SingletonCollector.tryCollect())
                   .orElseThrow(failure -> new IllegalArgumentException(constructError()));
    }

    private static String constructError() {
        return String.format(ERROR_MESSAGE_TEMPLATE, collectValueString());
    }

    private static String collectValueString() {
        return stream(values()).map(ApprovalStatus::getStatusValue).collect(Collectors.joining(DELIMITER));
    }

    /**
     * Lookup ApplicationCode by value. If value not supported, returns null.
     */
    public static ApprovalStatus fromValue(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getStatusValue().equalsIgnoreCase(value))
                   .collect(SingletonCollector.collectOrElse(null));
    }

}
