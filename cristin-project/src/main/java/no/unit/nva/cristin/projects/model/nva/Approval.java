package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.JsonPropertyNames.IDENTIFIER;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.Instant;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class Approval implements JsonSerializable {

    public static final String APPROVAL_DATE = "date";
    public static final String APPROVAL_AUTHORITY = "authority";
    public static final String APPROVAL_STATUS = "status";
    public static final String APPLICATION_CODE = "applicationCode";
    @JsonProperty(APPROVAL_DATE)
    private final Instant date;
    @JsonProperty(APPROVAL_AUTHORITY)
    private final ApprovalAuthority authority;
    @JsonProperty(APPROVAL_STATUS)
    private final ApprovalStatus status;
    @JsonProperty(APPLICATION_CODE)
    private final ApplicationCode applicationCode;
    @JsonProperty(IDENTIFIER)
    private final String identifier;

    @JsonCreator
    public Approval(@JsonProperty(APPROVAL_DATE) Instant date,
                    @JsonProperty(APPROVAL_AUTHORITY) ApprovalAuthority authority,
                    @JsonProperty(APPROVAL_STATUS) ApprovalStatus status,
                    @JsonProperty(APPLICATION_CODE) ApplicationCode applicationCode,
                    @JsonProperty(IDENTIFIER) String identifier) {
        this.date = date;
        this.authority = authority;
        this.status = status;
        this.applicationCode = applicationCode;
        this.identifier = identifier;
    }

    public Instant getDate() {
        return date;
    }

    public ApprovalAuthority getAuthority() {
        return authority;
    }

    public ApprovalStatus getStatus() {
        return status;
    }

    public ApplicationCode getApplicationCode() {
        return applicationCode;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return toJsonString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Approval)) {
            return false;
        }
        Approval approval = (Approval) o;
        return Objects.equals(getDate(), approval.getDate())
               && getAuthority() == approval.getAuthority()
               && getStatus() == approval.getStatus()
               && getApplicationCode() == approval.getApplicationCode()
               && Objects.equals(getIdentifier(), approval.getIdentifier());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDate(), getAuthority(), getStatus(), getApplicationCode(), getIdentifier());
    }
}
