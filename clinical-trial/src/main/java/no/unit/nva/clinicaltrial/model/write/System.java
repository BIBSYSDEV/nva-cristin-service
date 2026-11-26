package no.unit.nva.clinicaltrial.model.write;

public record System(String process) implements Actor {

  @Override
  public String name() {
    return process;
  }

  @Override
  public WriterKind kind() {
    return WriterKind.SYSTEM;
  }
}
