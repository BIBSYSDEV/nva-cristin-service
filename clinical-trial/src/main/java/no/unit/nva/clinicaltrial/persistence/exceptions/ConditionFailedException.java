package no.unit.nva.clinicaltrial.persistence.exceptions;

public class ConditionFailedException extends Exception {

  private static final String MESSAGE = "Item does not meet the condition of the write";

  private final transient Long version;

  public ConditionFailedException(Long storedVersion) {
    super(MESSAGE);
    this.version = storedVersion;
  }

  public Long storedVersion() {
    return version;
  }
}
