package no.unit.nva.clinicaltrial.model;

import java.time.Instant;
import no.unit.nva.clinicaltrial.model.write.Writer;

public record Version<T>(T entity, Writer writer, Action action, Instant modifiedDate) {}
