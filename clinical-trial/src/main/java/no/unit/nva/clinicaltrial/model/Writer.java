package no.unit.nva.clinicaltrial.model;

/** Whoever performed a write, recorded on every version. */
public interface Writer {

  String name();

  WriterKind kind();
}
