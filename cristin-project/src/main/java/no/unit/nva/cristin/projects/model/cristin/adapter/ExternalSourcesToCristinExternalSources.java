package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.model.ExternalSource;

public class ExternalSourcesToCristinExternalSources
    implements Function<List<ExternalSource>, List<CristinExternalSource>> {

    @Override
    public List<CristinExternalSource> apply(List<ExternalSource> externalSources) {
        return Optional.ofNullable(externalSources)
                   .map(this::extractExternalSources)
                   .orElse(null);
    }

    private List<CristinExternalSource> extractExternalSources(List<ExternalSource> externalSources) {
        return externalSources.stream()
                   .map(CristinExternalSource::fromExternalSource)
                   .toList();
    }

}
