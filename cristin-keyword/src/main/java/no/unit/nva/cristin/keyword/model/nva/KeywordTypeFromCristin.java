package no.unit.nva.cristin.keyword.model.nva;

import java.util.function.Function;
import no.unit.nva.cristin.model.CristinTypedLabel;

public class KeywordTypeFromCristin implements Function<CristinTypedLabel, KeywordType> {

    @Override
    public KeywordType apply(CristinTypedLabel cristinTypedLabel) {
        return new KeywordType(cristinTypedLabel.getCode(), cristinTypedLabel.getName());
    }

}
