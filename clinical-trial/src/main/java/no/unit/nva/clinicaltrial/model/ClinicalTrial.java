package no.unit.nva.clinicaltrial.model;

public record ClinicalTrial(String projectIdentifier, Reportable reportable, Long version)
    implements Entity {}
