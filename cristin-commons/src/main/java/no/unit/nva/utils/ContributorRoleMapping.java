package no.unit.nva.utils;

import nva.commons.core.SingletonCollector;

import java.util.Arrays;
import java.util.Optional;

public enum ContributorRoleMapping {

    MANAGER("ProjectManager", "PRO_MANAGER"),
    PARTICIPANT("ProjectParticipant","PRO_PARTICIPANT");

    private final String nvaRole;
    private final String cristinRole;

    ContributorRoleMapping(String nvaRole, String cristinRole) {
        this.nvaRole = nvaRole;
        this.cristinRole = cristinRole;
    }

    private String getNvaRole() {
        return nvaRole;
    }

    /**
     * Maps a role from cristin to NVA.
     * @param role cristinRole to map
     * @return corresponding role in NVA
     */
    public static Optional<String> getNvaRole(String role) {
        return Optional.ofNullable(Arrays.stream(values())
                .filter(contributorRoleMapping -> contributorRoleMapping.getCristinRole().equals(role))
                .map(ContributorRoleMapping::getNvaRole)
                .collect(SingletonCollector.collectOrElse(null)));
    }

    private String getCristinRole() {
        return cristinRole;
    }

    /**
     * Maps a role from NVA to Cristin.
     * @param role String representing role in NVA
     * @return role mapped to Cristin
     */
    public static Optional<String> getCristinRole(String role) {
        return Optional.ofNullable(Arrays.stream(ContributorRoleMapping.values())
                .filter(contributorRoleMapping -> contributorRoleMapping.getNvaRole().equals(role))
                .map(ContributorRoleMapping::getCristinRole)
                .collect(SingletonCollector.collectOrElse(null)));
    }
}
