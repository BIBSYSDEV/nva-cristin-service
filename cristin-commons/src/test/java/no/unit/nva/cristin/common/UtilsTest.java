package no.unit.nva.cristin.common;

import static no.unit.nva.commons.json.JsonUtils.dtoObjectMapper;
import static no.unit.nva.cristin.common.Utils.extractCristinInstitutionIdentifier;
import static nva.commons.apigateway.AccessRight.MANAGE_OWN_AFFILIATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.function.UnaryOperator;
import no.unit.nva.testutils.HandlerRequestBuilder;
import nva.commons.apigateway.RequestInfo;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.apigateway.exceptions.ForbiddenException;
import nva.commons.logutils.LogRecorder;
import org.junit.jupiter.api.Test;

class UtilsTest {

  private static final String THIRD_PARTY_SCOPE_PERSON_UPSERT =
      "https://api.nva.unit.no/scopes/third-party/person-upsert";
  private static final String BACKEND_SCOPE = "https://api.nva.unit.no/scopes/backend";
  private static final String FRONTEND_SCOPE = "https://api.nva.unit.no/scopes/frontend";
  private static final URI TOP_ORG =
      URI.create("https://api.dev.nva.aws.unit.no/cristin/organization/185.90.0.0");
  private static final URI CUSTOMER = URI.create("https://api.dev.nva.aws.unit.no/customer/123");
  private static final String CLIENT_ID = "some-client-id";
  private static final String CAN_UPDATE_ANY_INSTITUTION = "any";

  @Test
  void shouldReturnAnyInstitutionWhenClientIsInternalBackend() throws Exception {
    var requestInfo = request(builder -> builder.withScope(BACKEND_SCOPE));

    assertThat(extractCristinInstitutionIdentifier(requestInfo))
        .isEqualTo(CAN_UPDATE_ANY_INSTITUTION);
  }

  @Test
  void shouldReturnInstitutionOfTopLevelOrganizationWhenClientIsThirdParty() throws Exception {
    var requestInfo = thirdPartyRequest(TOP_ORG);

    assertThat(extractCristinInstitutionIdentifier(requestInfo)).isEqualTo("185");
  }

  @Test
  void shouldLogClientIdWhenClientIsThirdParty() throws Exception {
    var logRecorder = LogRecorder.forClass(Utils.class);
    var requestInfo = thirdPartyRequest(TOP_ORG);

    extractCristinInstitutionIdentifier(requestInfo);

    assertThat(logRecorder.asString()).contains("third party").contains(CLIENT_ID);
  }

  @Test
  void shouldThrowBadRequestWhenThirdPartyHasNoTopLevelOrganization() throws Exception {
    var requestInfo =
        request(
            builder -> builder.withScope(THIRD_PARTY_SCOPE_PERSON_UPSERT).withClientId(CLIENT_ID));

    assertThatThrownBy(() -> extractCristinInstitutionIdentifier(requestInfo))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void shouldThrowBadRequestWhenThirdPartyTopLevelOrganizationIsNotNumeric() throws Exception {
    var requestInfo = thirdPartyRequest(URI.create("https://example.org/organization/abc"));

    assertThatThrownBy(() -> extractCristinInstitutionIdentifier(requestInfo))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void shouldReturnInstitutionOfTopLevelOrganizationWhenUserIsUserAdministrator() throws Exception {
    var requestInfo =
        request(builder ->
            builder
                .withScope(FRONTEND_SCOPE)
                .withCurrentCustomer(CUSTOMER)
                .withAccessRights(CUSTOMER, MANAGE_OWN_AFFILIATION)
                .withTopLevelCristinOrgId(TOP_ORG));

    assertThat(extractCristinInstitutionIdentifier(requestInfo)).isEqualTo("185");
  }

  @Test
  void shouldThrowBadRequestWhenUserAdministratorHasNoTopLevelOrganization() throws Exception {
    var requestInfo =
        request(builder ->
            builder
                .withScope(FRONTEND_SCOPE)
                .withCurrentCustomer(CUSTOMER)
                .withAccessRights(CUSTOMER, MANAGE_OWN_AFFILIATION));

    assertThatThrownBy(() -> extractCristinInstitutionIdentifier(requestInfo))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void shouldThrowForbiddenWhenUserIsNotUserAdministrator() throws Exception {
    var requestInfo =
        request(builder ->
            builder
                .withScope(FRONTEND_SCOPE)
                .withCurrentCustomer(CUSTOMER)
                .withTopLevelCristinOrgId(TOP_ORG));

    assertThatThrownBy(() -> extractCristinInstitutionIdentifier(requestInfo))
        .isInstanceOf(ForbiddenException.class);
  }

  private static RequestInfo thirdPartyRequest(URI topLevelOrganization) throws Exception {
    return request(builder ->
        builder
            .withScope(THIRD_PARTY_SCOPE_PERSON_UPSERT)
            .withClientId(CLIENT_ID)
            .withTopLevelCristinOrgId(topLevelOrganization));
  }

  private static RequestInfo request(UnaryOperator<HandlerRequestBuilder<Void>> customizer)
      throws Exception {
    var builder = customizer.apply(new HandlerRequestBuilder<>(dtoObjectMapper));
    return RequestInfo.fromRequest(builder.build());
  }
}
