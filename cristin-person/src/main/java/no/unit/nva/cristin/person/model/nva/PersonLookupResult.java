package no.unit.nva.cristin.person.model.nva;

import no.unit.nva.model.UriId;

/**
 * Result of looking up a person by national identity number. GUI users and internal backend get the
 * full {@link Person}, while third parties get a {@link ThirdPartyPersonLookupResponse}.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface PersonLookupResult extends UriId {}
