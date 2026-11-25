package no.unit.nva.clinicaltrial.model;

import java.time.Instant;
import no.unit.nva.clinicaltrial.model.write.Writer;
import no.unit.nva.identifiers.SortableIdentifier;

public record Version<T>(
    SortableIdentifier identifier, T entity, Writer writer, Action action, Instant modifiedDate) {}
