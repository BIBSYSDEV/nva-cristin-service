package no.unit.nva.cristin.keyword.model.nva.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.cristin.model.CristinTypedLabel;

public class KeywordFromCristin implements Function<CristinTypedLabel, Keyword> {

    private final String context;

    public KeywordFromCristin() {
        context = Keyword.DEFAULT_CONTEXT;
    }

    public KeywordFromCristin(String context) {
        this.context = context;
    }

    @Override
    public Keyword apply(CristinTypedLabel cristinTypedLabel) {
        return new Keyword.Builder()
                   .withContext(context)
                   .withIdentifier(cristinTypedLabel.getCode())
                   .withLabels(cristinTypedLabel.getName())
                   .build();
    }

}
