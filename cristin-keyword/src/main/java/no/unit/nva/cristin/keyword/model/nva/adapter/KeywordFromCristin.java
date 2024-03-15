package no.unit.nva.cristin.keyword.model.nva.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.cristin.model.CristinTypedLabel;

public class KeywordFromCristin implements Function<CristinTypedLabel, Keyword> {

    @Override
    public Keyword apply(CristinTypedLabel cristinTypedLabel) {
        return new Keyword(cristinTypedLabel.getCode(), cristinTypedLabel.getName());
    }

}
