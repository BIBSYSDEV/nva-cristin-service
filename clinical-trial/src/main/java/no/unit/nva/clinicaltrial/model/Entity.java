package no.unit.nva.clinicaltrial.model;

public sealed interface Entity permits ClinicalTrial {

  String identifier();

  Long version();

  /** What this state of the entity does, recorded on its version. */
  Action action();
}
