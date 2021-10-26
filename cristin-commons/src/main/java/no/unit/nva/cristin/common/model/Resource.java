package no.unit.nva.cristin.common.model;

//import com.fasterxml.jackson.annotation.JsonSubTypes;
//import com.fasterxml.jackson.annotation.JsonTypeInfo;

/*@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Organization", value = NvaProject.class),
    @JsonSubTypes.Type(name = "UnconfirmedOrganization", value = UnconfirmedOrganization.class)
})*/
public interface Resource {
}
