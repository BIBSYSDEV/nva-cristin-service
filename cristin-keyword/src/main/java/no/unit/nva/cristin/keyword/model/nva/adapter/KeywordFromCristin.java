package no.unit.nva.cristin.keyword.model.nva.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.cristin.model.CristinTypedLabel;

public class KeywordFromCristin implements Function<CristinTypedLabel, Keyword> {

    private final boolean useDefaultContext;

    public KeywordFromCristin() {
        useDefaultContext = true;
    }

    public KeywordFromCristin(boolean useDefaultContext) {
        this.useDefaultContext = useDefaultContext;
    }

    @Override
    public Keyword apply(CristinTypedLabel cristinTypedLabel) {
        var builder = new Keyword.Builder();
        if (useDefaultContext) {
            builder.withDefaultContext();
        } else {
            builder.withContext(null);
        }

        return builder.withIdentifier(cristinTypedLabel.getCode())
                   .withLabels(cristinTypedLabel.getName())
                   .build();
    }

}
