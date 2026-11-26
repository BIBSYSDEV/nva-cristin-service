package no.unit.nva.clinicaltrial.model.write;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
@JsonSubTypes({@Type(User.class), @Type(System.class)})
public interface Actor {

  @JsonIgnore
  String name();

  @JsonIgnore
  WriterKind kind();
}
