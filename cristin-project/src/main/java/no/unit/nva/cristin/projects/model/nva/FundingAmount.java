package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class FundingAmount implements JsonSerializable {

    public static final String CURRENCY = "currency";
    public static final String VALUE = "value";
    @JsonProperty(CURRENCY)
    private final transient String currency;
    @JsonProperty(VALUE)
    private final transient Double value;

    @JsonCreator
    public FundingAmount(@JsonProperty(CURRENCY) String currency, @JsonProperty(VALUE) Double value) {
        this.currency = currency;
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FundingAmount that)) {
            return false;
        }
        return Objects.equals(getCurrency(), that.getCurrency()) && Objects.equals(getValue(), that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrency(), getValue());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
