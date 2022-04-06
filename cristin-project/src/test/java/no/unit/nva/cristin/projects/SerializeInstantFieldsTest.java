package no.unit.nva.cristin.projects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.unit.nva.commons.json.JsonUtils;
import no.unit.nva.cristin.projects.model.cristin.CristinProject;
import no.unit.nva.cristin.projects.model.nva.NvaProject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static no.unit.nva.cristin.projects.RandomProjectDataGenerator.randomMinimalNvaProject;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializeInstantFieldsTest {

    public static final ObjectMapper OBJECT_MAPPER_NON_EMPTY = JsonUtils.dynamoObjectMapper;

    @Test
    void serializeRandomProjectWithLossLessInstantTest() throws JsonProcessingException {
        String sampleInstantStringWithZeroMillis = "2021-03-01T12:00:00.000Z";
        Instant instant = Instant.parse(sampleInstantStringWithZeroMillis);
        NvaProject nvaProject = randomMinimalNvaProject();
        nvaProject.setStartDate(instant);
        CristinProject cristinProject = new CristinProjectBuilder(nvaProject).build();
        String projectAsJson = OBJECT_MAPPER_NON_EMPTY.writeValueAsString(cristinProject);
        JsonNode tree = OBJECT_MAPPER_NON_EMPTY.readTree(projectAsJson);
        String convertedInstant = tree.get("start_date").asText();
        assertEquals(sampleInstantStringWithZeroMillis, convertedInstant);
    }
}

