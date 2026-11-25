package no.unit.nva.clinicaltrial.persistence.v1;

public class ItemNotFoundException extends Exception {

  private static final String MESSAGE = "Item does not exist";

  public ItemNotFoundException() {
    super(MESSAGE);
  }
}
