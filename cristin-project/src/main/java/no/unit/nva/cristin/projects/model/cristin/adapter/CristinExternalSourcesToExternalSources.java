package no.unit.nva.cristin.projects.model.cristin.adapter;

import static java.util.Objects.nonNull;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.CristinExternalSource;
import no.unit.nva.model.ExternalSource;

public class CristinExternalSourcesToExternalSources
    implements Function<List<CristinExternalSource>, List<ExternalSource>> {

    @Override
    public List<ExternalSource> apply(List<CristinExternalSource> cristinExternalSources) {
        return extractExternalSources(cristinExternalSources);
    }

    private List<ExternalSource> extractExternalSources(List<CristinExternalSource> cristinExternalSources) {
        return nonNull(cristinExternalSources)
                   ? cristinExternalSources.stream()
                         .map(this::toExternalSource)
                         .collect(Collectors.toList())
                   : null;
    }

    private ExternalSource toExternalSource(CristinExternalSource cristinExternalSource) {
        return new ExternalSource(cristinExternalSource.getSourceReferenceId(),
                                  cristinExternalSource.getSourceShortName());
    }

}
