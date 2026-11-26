package no.unit.nva.clinicaltrial.persistence.exceptions;

public class ItemNotFoundException extends Exception {

  private static final String MESSAGE = "Item does not exist";

  public ItemNotFoundException() {
    super(MESSAGE);
  }
}
