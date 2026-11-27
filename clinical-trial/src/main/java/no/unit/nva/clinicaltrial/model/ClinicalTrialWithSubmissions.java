package no.unit.nva.clinicaltrial.model;

import java.util.List;

public record ClinicalTrialWithSubmissions(
    ClinicalTrial clinicalTrial, List<Submission> submissions) {}
