package no.unit.nva.clinicaltrial.model;

public record SystemWriter(String process) implements Writer {

  @Override
  public String name() {
    return process;
  }

  @Override
  public WriterKind kind() {
    return WriterKind.SYSTEM;
  }
}
