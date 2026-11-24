package no.unit.nva.clinicaltrial.rest;

import static com.fasterxml.jackson.annotation.JsonFormat.Feature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;

import com.fasterxml.jackson.annotation.JsonFormat;
import no.unit.nva.clinicaltrial.model.Reportable;

public record ClinicalTrialRequest(
    @JsonFormat(with = READ_UNKNOWN_ENUM_VALUES_AS_NULL) Reportable reportable) {}
