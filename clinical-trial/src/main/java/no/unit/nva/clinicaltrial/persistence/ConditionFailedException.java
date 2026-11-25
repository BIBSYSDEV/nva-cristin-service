package no.unit.nva.clinicaltrial.persistence;

public class ConditionFailedException extends Exception {

  private static final String MESSAGE = "Clinical trial does not meet the condition of the write";

  public ConditionFailedException() {
    super(MESSAGE);
  }
}
