package no.unit.nva.clinicaltrial.rest;

import java.util.List;

public record SubmissionRequest(List<InclusionRequest> inclusions) {}
