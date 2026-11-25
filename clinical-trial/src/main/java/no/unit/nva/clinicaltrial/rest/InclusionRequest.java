package no.unit.nva.clinicaltrial.rest;

import java.net.URI;

public record InclusionRequest(URI institution, Integer count) {}
