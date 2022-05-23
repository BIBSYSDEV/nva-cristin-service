package no.unit.nva.utils;

import no.unit.nva.cognito.CognitoUtil;
import no.unit.nva.exception.UnauthorizedException;
import nva.commons.apigateway.RequestInfo;
import nva.commons.core.Environment;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static no.unit.nva.cognito.CognitoUtil.ADMIN_TESTUSER_ID_KEY;
import static no.unit.nva.cognito.CognitoUtil.ADMIN_TESTUSER_PASSWORD_KEY;
import static no.unit.nva.cristin.common.client.ApiClient.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integrationTest")
class AccessUtilsTest {

    static final String OLD_EXPIRED_TOKEN = "eyJraWQiOiJGV3ZkaFp2aE5WaXVKQkNhTTVFcDQ0ZWhBZ0szWGpWVFNPanl2bkhoUzlBPSIsImFsZyI6IlJTMjU2In0.eyJhdF9oYXNoIjoiLTBOOV9LSm43NTF0RjFoSlk3ZWhoQSIsImN1c3RvbTpmZWlkZVRhcmdldGVkSWQiOiIyMWQ2NWU3OTZiOGE5Y2NlMTkxMmFhY2I1YzU0YTM3ZjU0MzExZmQwIiwic3ViIjoiNDAyZDE4MmUtMmQxOC00MWJhLTllMzItZGU4ZTY0YzY0MWUyIiwiY3VzdG9tOm9yZ0xlZ2FsTmFtZSI6IlVuaXQiLCJjb2duaXRvOmdyb3VwcyI6WyJldS13ZXN0LTFfRE5SbURQdHhZX0ZlaWRlSWRlbnRpdHlQcm92aWRlciJdLCJjdXN0b206eWVhck9mQmlydGgiOiIxOTY0IiwiY3VzdG9tOm9yZ05hbWUiOiJVTklUIiwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmV1LXdlc3QtMS5hbWF6b25hd3MuY29tXC9ldS13ZXN0LTFfRE5SbURQdHhZIiwiY3VzdG9tOm9yZ051bWJlciI6Ik5POTE5NDc3ODIyIiwiY3VzdG9tOmFwcGxpY2F0aW9uIjoiTlZBIiwiY3VzdG9tOmFwcGxpY2F0aW9uUm9sZXMiOiJDcmVhdG9yLEluc3RpdHV0aW9uLWFkbWluIiwiaWRlbnRpdGllcyI6W3sidXNlcklkIjoiYjg0YjYwNzc5MDNjZTNlYWM5MmRlM2JmMDhlYmMxY2Q0NWE3YTQ4NiIsInByb3ZpZGVyTmFtZSI6IkZlaWRlSWRlbnRpdHlQcm92aWRlciIsInByb3ZpZGVyVHlwZSI6IlNBTUwiLCJpc3N1ZXIiOiJodHRwczpcL1wvaWRwLXRlc3QuZmVpZGUubm8iLCJwcmltYXJ5IjoidHJ1ZSIsImRhdGVDcmVhdGVkIjoiMTY0MTgxNjM4NzM5NCJ9XSwiYXV0aF90aW1lIjoxNjQ2OTA2Mjg4LCJleHAiOjE2NDY5MDk5NDMsImN1c3RvbTphY2Nlc3NSaWdodHMiOiJFRElUX09XTl9JTlNUSVRVVElPTl9VU0VSUyxSRUFEX0RPSV9SRVFVRVNULEVESVRfT1dOX0lOU1RJVFVUSU9OX1BST0pFQ1RTIiwiY3VzdG9tOmZlaWRlSWQiOiJzZ0B1bml0Lm5vIiwiaWF0IjoxNjQ2OTA2MzQzLCJqdGkiOiJhZTNmZWUxZS1lOTBkLTQ0MGItYTdjNi1hYThhYzQzYTkyOGUiLCJlbWFpbCI6InN2ZW5uLmdqZXRvQHVuaXQubm8iLCJjdXN0b206aWRlbnRpZmllcnMiOiJmZWlkZTpzZ0B1bml0Lm5vIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJjdXN0b206Y3Jpc3RpbklkIjoiaHR0cHM6XC9cL2FwaS5kZXYubnZhLmF3cy51bml0Lm5vXC9jcmlzdGluXC9vcmdhbml6YXRpb25cLzIwMjAyLjAuMC4wIiwiY3VzdG9tOnNjaG9vbExpc3QiOiJOTzkxOTQ3NzgyMiIsImNvZ25pdG86dXNlcm5hbWUiOiJGZWlkZUlkZW50aXR5UHJvdmlkZXJfYjg0YjYwNzc5MDNjZTNlYWM5MmRlM2JmMDhlYmMxY2Q0NWE3YTQ4NiIsImN1c3RvbTpvcmdFbWFpbCI6Im5vcmVwbHlAdW5pdC5ubyIsImdpdmVuX25hbWUiOiJTdmVubiIsIm9yaWdpbl9qdGkiOiJkZmQ1ODAwMi1jYzU3LTRjNmMtOWFkMi1iYjU2YTA5YjZjNTMiLCJhdWQiOiI0cWZodjNrbDlxY3Iya25zZmI4bGh1MXU0MCIsImN1c3RvbTpjdXN0b21lcklkIjoiaHR0cHM6XC9cL2FwaS5kZXYubnZhLmF3cy51bml0Lm5vXC9jdXN0b21lclwvZjU0YzhhYTktMDczYS00NmExLThmN2MtZGRlNjZjODUzOTM0IiwidG9rZW5fdXNlIjoiaWQiLCJuYW1lIjoiU3Zlbm4gR2pldMO4IiwiY3VzdG9tOmNvbW1vbk5hbWUiOiJTdmVubiBHamV0w7giLCJmYW1pbHlfbmFtZSI6IkdqZXTDuCIsImN1c3RvbTphZmZpbGlhdGlvbiI6IlttZW1iZXIsIGVtcGxveWVlLCBzdGFmZl0ifQ.imxtfAtBu-7pd-zf0cqSomOegBkpKmBmMXji80Ae4vM1WeKaoxCW4hYJpqIi0PcK3tfMab6hlPgq7k8DjqM3WuiG1d2IKvfOeuuapF9NU1p7o8I84b0q6a3NKX_PgwZkPSfP2z5DgTx8Oigkp-pJHVoCldCQ36O9TjeFa0kK-zs7Zq8kH9-NcV8eUpsvgmDju45yGFqUpyDJY7HDbfxXOwl4AaYiB4y9i8-LsNSqyPhWoolirsvvUsm5YTgW1lV5XltiwzhBLow4KpB21HDrIWfae09Y5E-fQYnkVmCEEn2q_lDuL9z6GK2eu_xom2jt2H-AZTNwywuc1GAdYi1S4A";

    @Test
    void validateIdentificationNumberAccess() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setHeaders(Map.of(AUTHORIZATION, getBearerToken(OLD_EXPIRED_TOKEN)));
        assertThrows(UnauthorizedException.class, () -> AccessUtils.requesterIsUserAdministrator(requestInfo));
    }

    @Test
    void validateIdentificationNumberAccessForAdminUser() {
        RequestInfo requestInfo = new RequestInfo();
        final String token = loginAdminTestUser();
        assertNotNull(token);
        requestInfo.setHeaders(Map.of(AUTHORIZATION, getBearerToken(token)));
        assertTrue(AccessUtils.requesterIsUserAdministrator(requestInfo));
    }

    private String getBearerToken(String token) {
        return "Bearer " + token;
    }

    private String loginAdminTestUser() {
        final Environment environment = new Environment();
        String feideUserName = environment.readEnv(ADMIN_TESTUSER_ID_KEY);
        assertNotNull(feideUserName);
        String password = environment.readEnv(ADMIN_TESTUSER_PASSWORD_KEY);
        assertNotNull(password);
        String clientAppId = AccessUtils.getTestClientAppId();
        assertNotNull(clientAppId);
        String userpoolId = AccessUtils.getUserPoolId();
        assertNotNull(userpoolId);

        return CognitoUtil.loginUser(feideUserName, password, clientAppId);
    }
}
