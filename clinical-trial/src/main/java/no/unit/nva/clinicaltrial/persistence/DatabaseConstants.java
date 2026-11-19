package no.unit.nva.clinicaltrial.persistence;

public final class DatabaseConstants {

  public static final String PARTITION_KEY = "PK";
  public static final String SORT_KEY = "SK";
  public static final String KEY_SEPARATOR = "#";
  public static final String EXPECTED_VERSION_VALUE = ":expectedVersion";
  public static final String PARTITION_KEY_VALUE = ":partitionKey";
  public static final String VERSION_PREFIX_VALUE = ":versionPrefix";
  public static final String VERSIONS_KEY_CONDITION =
      "PK = :partitionKey AND begins_with(SK, :versionPrefix)";
  public static final String CREATE_CONDITION = "attribute_not_exists(PK)";
  public static final String UPDATE_CONDITION =
      "attribute_exists(PK) AND version = :expectedVersion";
  public static final String ITEM_NOT_SERIALIZABLE = "Item is not serializable";
  public static final String ITEM_NOT_READABLE = "Item is not readable";

  private DatabaseConstants() {}
}
