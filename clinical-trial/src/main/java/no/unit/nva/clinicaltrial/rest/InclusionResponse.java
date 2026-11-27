package no.unit.nva.clinicaltrial.rest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.net.URI;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonTypeName("Inclusion")
public record InclusionResponse(URI institution, Integer count) {}
