package no.unit.nva.cristin.person.update;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static no.unit.nva.cristin.model.Constants.OBJECT_MAPPER;
import static no.unit.nva.cristin.model.JsonPropertyNames.CRISTIN_EMPLOYMENTS;
import static no.unit.nva.cristin.model.JsonPropertyNames.FIRST_NAME;
import static no.unit.nva.cristin.model.JsonPropertyNames.ID;
import static no.unit.nva.cristin.model.JsonPropertyNames.LAST_NAME;
import static no.unit.nva.cristin.person.model.cristin.CristinPerson.PERSON_NVI;
import static no.unit.nva.cristin.person.model.nva.ContactDetails.EMAIL;
import static no.unit.nva.cristin.person.model.nva.ContactDetails.TELEPHONE;
import static no.unit.nva.cristin.person.model.nva.ContactDetails.WEB_PAGE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.AWARDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.BACKGROUND;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.COLLABORATION;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CONTACT_DETAILS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.COUNTRIES;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CRISTIN_TELEPHONE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.CRISTIN_WEB_PAGE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.EMPLOYMENTS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.KEYWORDS;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.NVI;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.ORCID;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PLACE;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_FIRST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.PREFERRED_LAST_NAME;
import static no.unit.nva.cristin.person.model.nva.JsonPropertyNames.RESERVED;
import static nva.commons.core.attempt.Try.attempt;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import no.unit.nva.cristin.model.CristinTypedLabel;
import no.unit.nva.cristin.model.CristinUnit;
import no.unit.nva.cristin.person.model.cristin.CristinNviInstitutionUnit;
import no.unit.nva.cristin.person.model.cristin.CristinPersonEmployment;
import no.unit.nva.cristin.person.model.cristin.CristinPersonNvi;
import no.unit.nva.cristin.person.model.cristin.CristinPersonSummary;
import no.unit.nva.cristin.person.model.cristin.CristinPersonSummary.Builder;
import no.unit.nva.cristin.person.model.nva.Award;
import no.unit.nva.cristin.person.model.nva.Employment;
import no.unit.nva.cristin.person.model.nva.PersonNvi;
import no.unit.nva.cristin.person.model.nva.PersonSummary;
import no.unit.nva.cristin.person.model.nva.TypedValue;
import no.unit.nva.cristin.person.model.nva.adapter.AwardToCristinFormat;
import no.unit.nva.model.Organization;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.utils.UriUtils;

public class CristinPersonPatchJsonCreator {

    public static final String CRISTIN_FIRST_NAME = "first_name";
    public static final String CRISTIN_SURNAME = "surname";
    public static final String CRISTIN_FIRST_NAME_PREFERRED = "first_name_preferred";
    public static final String CRISTIN_SURNAME_PREFERRED = "surname_preferred";

    private final transient ObjectNode input;
    private final transient ObjectNode output;

    /**
     * Class for creating json matching Cristin schema.
     */
    public CristinPersonPatchJsonCreator(ObjectNode input) {
        this.input = input;
        output = OBJECT_MAPPER.createObjectNode();
    }

    /**
     * Create Cristin json object.
     */
    public CristinPersonPatchJsonCreator create() {
        addOrcid();
        addFirstName();
        addLastName();
        addPreferredFirstName();
        addPreferredLastName();
        addReserved();
        addEmployments();
        addKeywords();
        addBackgroundIfPresent();
        addNviIfPresent();
        addContactDetailsIfPresent();
        addPlaceIfPresent();
        addCollaborationIfPresent();
        addCountriesIfPresent();
        addAwardsIfPresent();

        return this;
    }

    /**
     * Create Cristin json object from values allowed to be changed by the user themselves.
     */
    public CristinPersonPatchJsonCreator createWithAllowedUserModifiableData() {
        addOrcid();
        addPreferredFirstName();
        addPreferredLastName();
        addKeywords();
        addBackgroundIfPresent();
        addContactDetailsIfPresent();
        addPlaceIfPresent();
        addCollaborationIfPresent();
        addCountriesIfPresent();
        addAwardsIfPresent();

        return this;
    }

    public ObjectNode getOutput() {
        return output;
    }

    private void addOrcid() {
        if (input.has(ORCID)) {
            ObjectNode identifier = OBJECT_MAPPER.createObjectNode();
            identifier.set(ID, input.get(ORCID));
            output.set(ORCID, identifier);
        }
    }

    private void addFirstName() {
        if (input.has(FIRST_NAME)) {
            output.set(CRISTIN_FIRST_NAME, input.get(FIRST_NAME));
        }
    }

    private void addLastName() {
        if (input.has(LAST_NAME)) {
            output.set(CRISTIN_SURNAME, input.get(LAST_NAME));
        }
    }

    private void addPreferredFirstName() {
        if (input.has(PREFERRED_FIRST_NAME)) {
            output.set(CRISTIN_FIRST_NAME_PREFERRED, input.get(PREFERRED_FIRST_NAME));
        }
    }

    private void addPreferredLastName() {
        if (input.has(PREFERRED_LAST_NAME)) {
            output.set(CRISTIN_SURNAME_PREFERRED, input.get(PREFERRED_LAST_NAME));
        }
    }

    private void addReserved() {
        if (input.has(RESERVED) && input.get(RESERVED).asBoolean()) {
            output.set(RESERVED, input.get(RESERVED));
        }
    }

    
    private void addEmployments() {
        if (input.has(EMPLOYMENTS) && !input.get(EMPLOYMENTS).isNull()) {
            var employmentsInCristinFormat =
                attempt(() -> {
                    var parsedInput =
                        asList(OBJECT_MAPPER.readValue(input.get(EMPLOYMENTS).toString(), Employment[].class));
                    return OBJECT_MAPPER.readTree(employmentsToCristinFormat(parsedInput).toString());
                }).orElseThrow();
            output.set(CRISTIN_EMPLOYMENTS, employmentsInCristinFormat);
        }
    }

    private List<CristinPersonEmployment> employmentsToCristinFormat(List<Employment> employments) {
        return employments.stream().map(Employment::toCristinEmployment).collect(Collectors.toList());
    }

    
    private void addKeywords() {
        if (input.has(KEYWORDS) && !input.get(KEYWORDS).isNull()) {
            var keywordsInCristinFormat =
                attempt(() -> {
                    var parsedInput =
                        asList(OBJECT_MAPPER.readValue(input.get(KEYWORDS).toString(), TypedValue[].class));
                    return OBJECT_MAPPER.readTree(keywordsToCristinFormat(parsedInput).toString());
                }).orElseThrow();
            output.set(KEYWORDS, keywordsInCristinFormat);
        }
    }

    private List<CristinTypedLabel> keywordsToCristinFormat(List<TypedValue> parsedInput) {
        return parsedInput.stream().map(elm -> new CristinTypedLabel(elm.type(), null))
                   .collect(Collectors.toList());
    }

    private void addBackgroundIfPresent() {
        if (input.has(BACKGROUND)) {
            output.set(BACKGROUND, input.get(BACKGROUND));
        }
    }

    private void addNviIfPresent() {
        if (input.has(NVI)) {
            if (input.get(NVI).isNull()) {
                output.putNull(PERSON_NVI);
                return;
            }

            var nviNodeString = input.get(NVI).toString();

            var nviInCristinFormat = attempt(() -> {
                var parsedInput = OBJECT_MAPPER.readValue(nviNodeString, PersonNvi.class);
                return OBJECT_MAPPER.readTree(nviToCristinFormat(parsedInput).toJsonString());
            }).orElseThrow();

            output.set(PERSON_NVI, nviInCristinFormat);
        }
    }

    private void addContactDetailsIfPresent() {
        if (input.has(CONTACT_DETAILS) && !input.get(CONTACT_DETAILS).isNull()) {
            addTelephoneIfPresent();
            addEmailIfPresent();
            addWebPageIfPresent();
        }
    }

    private void addTelephoneIfPresent() {
        var contactDetails = input.get(CONTACT_DETAILS);

        if (contactDetails.has(TELEPHONE)) {
            if (contactDetails.get(TELEPHONE).isNull()) {
                output.putNull(CRISTIN_TELEPHONE);
            } else {
                output.put(CRISTIN_TELEPHONE, contactDetails.get(TELEPHONE).asText());
            }
        }
    }

    private void addEmailIfPresent() {
        var contactDetails = input.get(CONTACT_DETAILS);

        if (contactDetails.has(EMAIL)) {
            if (contactDetails.get(EMAIL).isNull()) {
                output.putNull(EMAIL);
            } else {
                output.put(EMAIL, contactDetails.get(EMAIL).asText());
            }
        }
    }

    private void addWebPageIfPresent() {
        var contactDetails = input.get(CONTACT_DETAILS);

        if (contactDetails.has(WEB_PAGE)) {
            if (contactDetails.get(WEB_PAGE).isNull()) {
                output.putNull(CRISTIN_WEB_PAGE);
            } else {
                output.put(CRISTIN_WEB_PAGE, contactDetails.get(WEB_PAGE).asText());
            }
        }
    }

    private CristinPersonNvi nviToCristinFormat(PersonNvi parsedInput) {
        var unitId = extractNviUnitId(parsedInput);
        var unit = unitId.map(CristinUnit::fromCristinUnitIdentifier)
                       .map(id -> new CristinNviInstitutionUnit(null, id));

        var personId = extractNviPersonId(parsedInput);
        var person = personId.map(id -> CristinPersonSummary.builder().withCristinPersonId(id))
                         .map(Builder::build);

        return new CristinPersonNvi(person.orElse(null), unit.orElse(null), null);
    }

    private static Optional<String> extractNviPersonId(PersonNvi parsedInput) {
        return Optional.ofNullable(parsedInput)
                   .map(PersonNvi::verifiedBy)
                   .map(PersonSummary::id)
                   .map(UriUtils::extractLastPathElement);
    }

    private static Optional<String> extractNviUnitId(PersonNvi parsedInput) {
        return Optional.ofNullable(parsedInput)
                   .map(PersonNvi::verifiedAt)
                   .map(Organization::getId)
                   .map(UriUtils::extractLastPathElement);
    }

    private void addPlaceIfPresent() {
        if (input.has(PLACE)) {
            output.set(PLACE, input.get(PLACE));
        }
    }

    private void addCollaborationIfPresent() {
        if (input.has(COLLABORATION)) {
            output.set(COLLABORATION, input.get(COLLABORATION));
        }
    }

    private void addCountriesIfPresent() {
        if (input.has(COUNTRIES) && !input.get(COUNTRIES).isNull()) {
            var countriesInCristinFormat =
                attempt(() -> {
                    var parsedInput =
                        asList(OBJECT_MAPPER.readValue(input.get(COUNTRIES).toString(), TypedLabel[].class));
                    return OBJECT_MAPPER.readTree(typedLabelsToCristinFormat(parsedInput).toString());
                }).orElseThrow();
            output.set(COUNTRIES, countriesInCristinFormat);
        }
    }

    private List<CristinTypedLabel> typedLabelsToCristinFormat(List<TypedLabel> typedLabels) {
        return typedLabels.stream()
                   .filter(elm -> nonNull(elm.getType()))
                   .map(elm -> new CristinTypedLabel(elm.getType().toUpperCase(Locale.ROOT), null))
                   .collect(Collectors.toList());
    }

    private void addAwardsIfPresent() {
        if (input.has(AWARDS)) {
            if (input.get(AWARDS).isNull()) {
                output.putNull(AWARDS);
                return;
            }

            var awards = attempt(() -> asList(OBJECT_MAPPER.readValue(input.get(AWARDS).toString(), Award[].class)))
                             .orElseThrow();
            var cristinAwards = awards.stream().map(new AwardToCristinFormat()).collect(Collectors.toSet());
            var cristinAwardsNode = attempt(() -> OBJECT_MAPPER.readTree(cristinAwards.toString())).orElseThrow();

            output.set(AWARDS, cristinAwardsNode);
        }
    }
}
