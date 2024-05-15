package no.unit.nva.model.adapter;

import static java.util.Objects.nonNull;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.model.TypedLabel;

public class TypedLabelToCristinFormat implements Function<TypedLabel, CristinTypedLabel> {

    @Override
    public CristinTypedLabel apply(TypedLabel typedLabel) {
        return nonNull(typedLabel) ? new CristinTypedLabel(typedLabel.getType(), typedLabel.getLabel()) : null;
    }

}
