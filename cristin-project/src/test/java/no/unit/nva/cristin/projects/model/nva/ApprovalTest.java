package no.unit.nva.cristin.projects.model.nva;

import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class ApprovalTest {

    @Test
    void shouldDeserializeEnumsWithPartialMissingValues() throws JsonProcessingException {
        String approvalString = "{\n"
                                + "\"type\" : \"Approval\",\n"
                                + "\"date\" : \"2009-03-13T09:17:50.326Z\",\n"
                                + "\"authority\" : \"DIRHEALTH\",\n"
                                + "\"identifier\" : \"lwV7zz35TaM75kn\"\n"
                                + "}";

        Approval approval = OBJECT_MAPPER.readValue(approvalString, Approval.class);

        assertThat(approval, notNullValue());
    }

}
