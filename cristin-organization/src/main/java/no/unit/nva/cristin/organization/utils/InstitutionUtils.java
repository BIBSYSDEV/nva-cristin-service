package no.unit.nva.cristin.organization.utils;

import no.unit.nva.cristin.common.model.SearchResponse;
import no.unit.nva.cristin.model.nva.Organization;
import no.unit.nva.cristin.organization.dto.InstitutionDto;
import nva.commons.core.JsonUtils;

import java.io.IOException;
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
    public static SearchResponse<Organization> toInstitutionListResponse(String institutionsJson)
            throws IOException {
        try {
            List<InstitutionDto> institutions = Arrays.asList(
                    JsonUtils.dtoObjectMapper.readValue(institutionsJson, InstitutionDto[].class));
            return new SearchResponse<Organization>(null).withHits(institutions
                    .stream()
                    .map(InstitutionUtils::toInstitutionResponse)
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            throw new IOException(PARSE_ERROR + institutionsJson, e);
        }
    }

    private static Organization toInstitutionResponse(InstitutionDto institutionDto) {
        return new Organization.Builder()
                .withId(institutionDto.getUri())
                .withName(institutionDto.getName())
                .withAcronym(institutionDto.getAcronym())
                .build();
    }

}
