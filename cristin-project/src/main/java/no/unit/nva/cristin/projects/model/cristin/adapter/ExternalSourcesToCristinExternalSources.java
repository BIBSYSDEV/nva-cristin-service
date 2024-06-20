package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.nonNull;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.model.ExternalSource;

public class ExternalSourcesToCristinExternalSources
    implements Function<List<ExternalSource>, List<CristinExternalSource>> {

    @Override
    public List<CristinExternalSource> apply(List<ExternalSource> externalSources) {
        return nonNull(externalSources) ? extractExternalSources(externalSources) : null;
    }

    private List<CristinExternalSource> extractExternalSources(List<ExternalSource> externalSources) {
        return externalSources.stream()
                   .map(this::toCristinExternalSource)
                   .collect(Collectors.toList());
    }

    private CristinExternalSource toCristinExternalSource(ExternalSource externalSource) {
        return new CristinExternalSource(externalSource.getName(), externalSource.getIdentifier());
    }

}
