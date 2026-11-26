package no.unit.nva.clinicaltrial.model;

import no.unit.nva.identifiers.OrganizationIdentifier;

public record Inclusion(OrganizationIdentifier institutionIdentifier, Integer count) {}
