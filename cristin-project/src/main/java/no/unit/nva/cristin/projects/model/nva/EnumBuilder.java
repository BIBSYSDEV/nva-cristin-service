package no.unit.nva.cristin.projects.model.nva;

public interface EnumBuilder<T, R extends Enum<R>> {

    R build(T classOfT);

}
