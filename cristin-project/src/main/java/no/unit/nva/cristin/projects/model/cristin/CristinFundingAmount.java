package no.unit.nva.cristin.projects.model.cristin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import no.unit.nva.cristin.projects.model.nva.FundingAmount;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CristinFundingAmount(@JsonProperty(CRISTIN_CURRENCY_CODE) String currencyCode,
                                   @JsonProperty(AMOUNT) Double amount) {

    public static final String CRISTIN_CURRENCY_CODE = "currency_code";
    public static final String AMOUNT = "amount";

    public FundingAmount toFundingAmount() {
        return new FundingAmount(currencyCode(), amount());
    }

}
