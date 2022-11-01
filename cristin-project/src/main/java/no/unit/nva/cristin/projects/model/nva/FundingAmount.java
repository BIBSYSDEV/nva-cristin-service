package no.unit.nva.cristin.projects.model.nva;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static no.unit.nva.cristin.model.JsonPropertyNames.AMOUNT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import no.unit.nva.commons.json.JsonSerializable;

@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public class FundingAmount implements JsonSerializable {

    public static final String CURRENCY_CODE = "currencyCode";
    private final transient String currencyCode;
    private final transient Double amount;

    @JsonCreator
    public FundingAmount(@JsonProperty(CURRENCY_CODE) String currencyCode, @JsonProperty(AMOUNT) Double amount) {
        this.currencyCode = currencyCode;
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Double getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FundingAmount)) {
            return false;
        }
        FundingAmount that = (FundingAmount) o;
        return Objects.equals(getCurrencyCode(), that.getCurrencyCode()) && Objects.equals(getAmount(),
                                                                                           that.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrencyCode(), getAmount());
    }

    @Override
    public String toString() {
        return toJsonString();
    }

}
