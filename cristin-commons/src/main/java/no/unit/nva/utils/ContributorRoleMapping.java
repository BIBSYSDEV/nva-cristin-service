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

    public static String getNvaRole(String role) {
        if (mMap == null) {
            initializeMapping();
        }
        if (mMap.containsKey(role)) {
            return mMap.get(role);
        }
        return null;
    }

    public static String getCristinRole(String role) {
        if (mMap == null) {
            initializeMapping();
        }
        if (mMap.containsKey(role)) {
            return mMap.get(role);
        }
        return null;
    }

    private static void initializeMapping() {
        mMap = new ConcurrentHashMap<>();
        for (ContributorRoleMapping s : ContributorRoleMapping.values()) {
            mMap.put(s.nvaRole, s.cristinRole);
            mMap.put(s.cristinRole, s.nvaRole);
        }
    }

}
