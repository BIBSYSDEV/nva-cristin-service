package no.unit.nva.cristin.person.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.nio.file.Path;
import no.unit.nva.cristin.person.model.nva.Person;
import no.unit.nva.cristin.testing.HttpResponseFaker;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.NotFoundException;
import nva.commons.core.ioutils.IoUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CristinPersonApiClientTest {

  private static final String SYNTHETIC_IDENTITY_NUMBER = "08923340832";
  private static final String CRISTIN_GET_PERSON_RESPONSE_JSON = "cristinGetPersonResponse.json";
  private static final String CRISTIN_RESERVED_FIELD = "reserved";
  private static final String EMPTY_ARRAY = "[]";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private CristinPersonApiClient apiClient;

  @BeforeEach
  void setUp() {
    apiClient = spy(new CristinPersonApiClientStub());
  }

  @Test
  void shouldReturnPersonFromNinLookupWhenPersonIsNotReserved() throws Exception {
    var person = lookUpPerson();

    assertThat(person.id()).isNotNull();
  }

  @Test
  void shouldThrowNotFoundFromNinLookupWhenPersonIsReserved() throws Exception {
    returnReservedPersonFromCristin();

    assertThatThrownBy(this::lookUpPerson).isInstanceOf(NotFoundException.class);
  }

  @Test
  void shouldThrowSameNotFoundFromNinLookupForReservedPersonAsForNoMatch() throws Exception {
    returnReservedPersonFromCristin();
    var reserved = catchThrowable(this::lookUpPerson);

    doReturn(new HttpResponseFaker(EMPTY_ARRAY, 200))
        .when(apiClient)
        .fetchQueryResults(any(URI.class));
    var noMatch = catchThrowable(this::lookUpPerson);

    assertThat(reserved).isInstanceOf(NotFoundException.class);
    assertThat(reserved.getMessage()).isEqualTo(noMatch.getMessage());
  }

  private Person lookUpPerson() throws ApiGatewayException {
    return apiClient.getPersonFromNationalIdentityNumber(SYNTHETIC_IDENTITY_NUMBER);
  }

  private void returnReservedPersonFromCristin() throws Exception {
    var reservedPerson =
        (ObjectNode)
            OBJECT_MAPPER.readTree(
                IoUtils.stringFromResources(Path.of(CRISTIN_GET_PERSON_RESPONSE_JSON)));
    reservedPerson.put(CRISTIN_RESERVED_FIELD, true);
    doReturn(new HttpResponseFaker(reservedPerson.toString(), 200))
        .when(apiClient)
        .fetchGetResult(any(URI.class));
  }
}
