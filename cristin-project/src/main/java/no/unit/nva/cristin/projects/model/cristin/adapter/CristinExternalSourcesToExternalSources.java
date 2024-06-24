package no.unit.nva.cristin.projects.model.cristin.adapter;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.model.ExternalSource;

public class CristinExternalSourcesToExternalSources
    implements Function<List<CristinExternalSource>, List<ExternalSource>> {

    @Override
    public List<ExternalSource> apply(List<CristinExternalSource> cristinExternalSources) {
        return Optional.ofNullable(cristinExternalSources)
                   .map(this::extractExternalSources)
                   .orElse(null);
    }

    private List<ExternalSource> extractExternalSources(List<CristinExternalSource> cristinExternalSources) {
        return cristinExternalSources.stream()
                   .map(CristinExternalSource::toExternalSource)
                   .toList();
    }



}
