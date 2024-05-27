package no.unit.nva.cristin.keyword.model.nva.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.cristin.model.CristinTypedLabel;

public class KeywordToCristin implements Function<Keyword, CristinTypedLabel> {

    @Override
    public CristinTypedLabel apply(Keyword keyword) {
        return new CristinTypedLabel(null, keyword.getLabels());
    }

}
