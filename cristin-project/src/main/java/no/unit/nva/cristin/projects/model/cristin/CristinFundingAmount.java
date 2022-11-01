package no.unit.nva.cristin.projects.model.cristin;

import static no.unit.nva.cristin.model.JsonPropertyNames.AMOUNT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinFundingAmount {

    public static final String CRISTIN_CURRENCY_CODE = "currency_code";

    @JsonProperty(CRISTIN_CURRENCY_CODE)
    private final transient String currencyCode;
    @JsonProperty(AMOUNT)
    private final transient Double amount;

    @JsonCreator
    public CristinFundingAmount(@JsonProperty(CRISTIN_CURRENCY_CODE) String currencyCode,
                                @JsonProperty(AMOUNT) Double amount) {
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
        if (!(o instanceof CristinFundingAmount)) {
            return false;
        }
        CristinFundingAmount that = (CristinFundingAmount) o;
        return Objects.equals(getCurrencyCode(), that.getCurrencyCode()) && Objects.equals(getAmount(),
                                                                                           that.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrencyCode(), getAmount());
    }

}
