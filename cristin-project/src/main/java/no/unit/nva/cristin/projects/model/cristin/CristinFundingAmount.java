package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Objects;
import no.unit.nva.cristin.projects.model.nva.FundingAmount;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CristinFundingAmount {

    public static final String CRISTIN_CURRENCY_CODE = "currency_code";
    public static final String AMOUNT = "amount";

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

    public FundingAmount toFundingAmount() {
        return new FundingAmount(getCurrencyCode(), getAmount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CristinFundingAmount that)) {
            return false;
        }
        return Objects.equals(getCurrencyCode(), that.getCurrencyCode()) && Objects.equals(getAmount(),
                                                                                           that.getAmount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCurrencyCode(), getAmount());
    }

}
