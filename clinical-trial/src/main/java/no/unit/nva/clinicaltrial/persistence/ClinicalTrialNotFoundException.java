package no.unit.nva.clinicaltrial.persistence;

public class ClinicalTrialNotFoundException extends Exception {

  private static final String MESSAGE = "Clinical trial does not exist";

  public ClinicalTrialNotFoundException() {
    super(MESSAGE);
  }
}
