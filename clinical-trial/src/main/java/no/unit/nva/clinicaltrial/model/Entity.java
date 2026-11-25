package no.unit.nva.clinicaltrial.model;

public sealed interface Entity permits ClinicalTrial, Period {

  String identifier();

  Long version();

  Action action();
}
