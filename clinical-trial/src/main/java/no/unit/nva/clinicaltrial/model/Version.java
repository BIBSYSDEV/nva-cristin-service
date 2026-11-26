package no.unit.nva.clinicaltrial.model;

import java.time.Instant;
import no.unit.nva.clinicaltrial.model.write.Actor;

public record Version<T>(T entity, Actor actor, Action action, Instant modifiedDate) {}
