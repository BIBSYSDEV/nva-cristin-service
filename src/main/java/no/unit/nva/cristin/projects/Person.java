package no.unit.nva.cristin.projects;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Person {

    @SerializedName("cristin_person_id")
    public String cristinPersonId;
    @SerializedName("first_name")
    public String firstName;
    public String surname;
    public String tel;
    @SerializedName("identified_cristin_person")
    public Boolean identifiedCristinPerson;
    @SerializedName("cristin_profile_url")
    public String cristinProfileUrl;
    @SerializedName("picture_url")
    public String pictureUrl;
    public String url;
    public List<Role> roles;
    public List<Affiliation> affiliations;

}

