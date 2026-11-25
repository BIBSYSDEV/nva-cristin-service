package no.unit.nva.clinicaltrial.model;

public sealed interface Entity permits ClinicalTrial {

  String identifier();

  Long version();

  Action action();
}
