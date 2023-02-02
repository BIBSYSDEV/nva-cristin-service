package no.unit.nva.cristin.projects.model.nva;

import static java.util.Arrays.stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Collectors;
import nva.commons.core.SingletonCollector;

public enum ApplicationCode {

    ETHICHAPR("EtichAppr"),
    BIOBANK("Biobank"),
    DRUGTRIAL("DrugTrial"),
    BIOTECHN("BioTechN"),
    MEDEQUIP("MedEquip"),
    TESTANIMAL("TestAnimal"),
    SENSINFO("SensInfo");

    public static final String ERROR_MESSAGE_TEMPLATE = "Supplied ApplicationCode is not valid expected one of: %s";
    public static final String DELIMITER = ", ";

    private final String codeValue;

    ApplicationCode(String codeValue) {
        this.codeValue = codeValue;
    }

    @JsonValue
    public String getCodeValue() {
        return codeValue;
    }

    /**
     * Lookup ApplicationCode by json. If value not supported, throws exception.
     */
    @JsonCreator
    @SuppressWarnings("unused")
    public static ApplicationCode fromJson(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getCodeValue().equalsIgnoreCase(value))
                   .collect(SingletonCollector.tryCollect())
                   .orElseThrow(failure -> new IllegalArgumentException(constructError()));
    }

    private static String constructError() {
        return String.format(ERROR_MESSAGE_TEMPLATE, collectValueString());
    }

    private static String collectValueString() {
        return stream(values()).map(ApplicationCode::getCodeValue).collect(Collectors.joining(DELIMITER));
    }

    /**
     * Lookup ApplicationCode by value. If value not supported, returns null.
     */
    public static ApplicationCode fromValue(String value) {
        return stream(values())
                   .filter(nameType -> nameType.getCodeValue().equalsIgnoreCase(value))
                   .collect(SingletonCollector.collectOrElse(null));
    }

}
