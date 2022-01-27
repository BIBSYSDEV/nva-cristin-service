package no.unit.nva.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ContributorRoleMapping {

    MANAGER("ProjectManager", "PRO_MANAGER"),
    PARTICIPANT("ProjectParticipant","PRO_PARTICIPANT");

    private final String nvaRole;
    private final String cristinRole;
    private static Map<String, String> mMap;

    ContributorRoleMapping(String nvaRole, String cristinRole) {
        this.nvaRole = nvaRole;
        this.cristinRole = cristinRole;
    }

    /**
     * Maps a role from cristin to NVA.
     * @param role cristinRole to map
     * @return corresponding role in NVA
     */
    public static String getNvaRole(String role) {
        String result = null;
        if (mMap == null) {
            initializeMapping();
        }
        if (mMap.containsKey(role)) {
            result = mMap.get(role);
        }
        return result;
    }

    /**
     * Maps a role from NVA to Cristin.
     * @param role String representing role in NVA
     * @return role mapped to Cristin
     */
    public static String getCristinRole(String role) {
        String result = null;
        if (mMap == null) {
            initializeMapping();
        }
        if (mMap.containsKey(role)) {
            result = mMap.get(role);
        }
        return result;
    }

    private static void initializeMapping() {
        mMap = new ConcurrentHashMap<>();
        for (ContributorRoleMapping s : ContributorRoleMapping.values()) {
            mMap.put(s.nvaRole, s.cristinRole);
            mMap.put(s.cristinRole, s.nvaRole);
        }
    }

}
