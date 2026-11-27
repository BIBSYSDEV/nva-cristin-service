package no.unit.nva.clinicaltrial.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;
import no.unit.nva.clinicaltrial.model.SubmissionStatus;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonTypeName("Submission")
public record SubmissionSummary(URI id, String period, SubmissionStatus status) {}
