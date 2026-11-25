package no.unit.nva.clinicaltrial.model;

public sealed interface Entity permits ClinicalTrial, Period, Submission {

  Long version();

  Action action();
}
