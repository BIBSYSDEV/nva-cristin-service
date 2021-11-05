package no.unit.nva.cristin.organization.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.unit.nva.cristin.organization.InstitutionListResponse;
import no.unit.nva.cristin.organization.InstitutionResponse;
import no.unit.nva.cristin.organization.dto.InstitutionBaseDto;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import no.unit.nva.cristin.organization.dto.SubSubUnitDto;
import no.unit.nva.cristin.organization.dto.SubUnitDto;
import nva.commons.core.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class InstitutionUtils {

    public static final String NO_NAME = "No name";
    public static final String PARSE_ERROR = "Failed to parse: ";

    private InstitutionUtils() {
    }

    /**
     * Map external (Cristin) institutions to the internal model.
     *
     * @param institutionsJson jsonString as received from the external provider
     * @return a list of institutions
     * @throws IOException when the parsing of the JSON string fails.
     */
    public static InstitutionListResponse toInstitutionListResponse(String institutionsJson)
        throws IOException {
        try {
            List<InstitutionDto> institutions = Arrays.asList(
                    JsonUtils.dtoObjectMapper.readValue(institutionsJson, InstitutionDto[].class));
            return new InstitutionListResponse(institutions
                .stream()
                .map(InstitutionUtils::toInstitutionResponse)
                .collect(Collectors.toList()));
        } catch (IOException e) {
            throw new IOException(PARSE_ERROR + institutionsJson, e);
        }
    }

    private static InstitutionResponse toInstitutionResponse(InstitutionDto institutionDto) {
        return new InstitutionResponse.Builder()
            .withId(institutionDto.getUri())
            .withName(InstitutionUtils.getAnyName(institutionDto))
            .withAcronym(institutionDto.getAcronym())
            .build();
    }

    private static String getAnyName(InstitutionDto institutionDto) {
        return institutionDto.getName().values().stream().findFirst().orElse(NO_NAME);
    }

    /**
     * Map Cristin Institution model to object.
     *
     * @param json JSON string to be parsed
     * @return An InstitutionDto object
     * @throws IOException Thrown if the JSON cannot be parsed
     */
    public static InstitutionBaseDto toInstitutionBaseDto(String json) throws IOException {
        try {
            return JsonUtils.dtoObjectMapper.readValue(json, InstitutionBaseDto.class);
        } catch (JsonProcessingException e) {
            throw new IOException(PARSE_ERROR + json, e);
        }
    }

    /**
     * Generates a list of URIs from an array of Unit objects.
     *
     * @param json JSON string to be parsed
     * @return List of URIs
     * @throws IOException Thrown if the JSON cannot be parsed
     */
    public static List<URI> toUriList(String json) throws IOException {
        try {
            List<SubUnitDto> subUnitDtos = Arrays.asList(JsonUtils.dtoObjectMapper.readValue(json, SubUnitDto[].class));
            return subUnitDtos.stream().map(InstitutionUtils::getSubunitUri).collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new IOException(PARSE_ERROR + json, e);
        }
    }

    private static URI getSubunitUri(SubUnitDto unit) {
        return unit.getUri();
    }

    /**
     * Generate a Subunit from Cristin json.
     *
     * @param json JSON string for object
     * @return A SubUnitDto
     * @throws IOException Thrown if the JSON cannot be parsed
     */
    public static SubSubUnitDto toSubSubUnitDto(String json) throws IOException {
        try {
            return JsonUtils.dtoObjectMapper.readValue(json, SubSubUnitDto.class);
        } catch (JsonProcessingException e) {
            throw new IOException(PARSE_ERROR + json, e);
        }
    }
}
