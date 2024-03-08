package no.unit.nva.model.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.model.TypedLabel;

public class CristinTypedLabelToNvaFormat implements Function<CristinTypedLabel, TypedLabel> {

    @Override
    public TypedLabel apply(CristinTypedLabel cristinTypedLabel) {
        return new TypedLabel(cristinTypedLabel.getCode(), cristinTypedLabel.getName());
    }

}
