package no.unit.nva.clinicaltrial.persistence.v3;

public class ConditionFailedException extends Exception {

  private static final String MESSAGE = "Item does not meet the condition of the write";

  public ConditionFailedException() {
    super(MESSAGE);
  }
}
