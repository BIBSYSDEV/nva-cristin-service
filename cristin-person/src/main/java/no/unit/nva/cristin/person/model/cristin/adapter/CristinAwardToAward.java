package no.unit.nva.cristin.person.model.cristin.adapter;

import java.util.function.Function;
import no.unit.nva.cristin.person.model.cristin.CristinAward;
import no.unit.nva.cristin.person.model.nva.Award;
import no.unit.nva.model.Organization;
import no.unit.nva.model.TypedLabel;

public class CristinAwardToAward implements Function<CristinAward, Award> {

    private String title;
    private int year;
    private TypedLabel type;
    private TypedLabel distribution;
    private Organization affiliation;

    @Override
    public Award apply(CristinAward cristinAward) {
        return null;
    }

}
