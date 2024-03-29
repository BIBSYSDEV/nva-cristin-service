package no.unit.nva.biobank.model;

import static java.util.Objects.nonNull;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.INVALID;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.LANGUAGE;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.PATH_BIOBANK;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.PATH_IDENTITY;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.VALID_QUERY_PARAMETER_NVA_KEYS;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.keyFromString;
import static nva.commons.core.StringUtils.EMPTY_STRING;
import java.util.Set;
import no.unit.nva.cristin.model.QueryBuilder;

public class QueryBuilderBiobank extends QueryBuilder<ParameterKeyBiobank> {

    public QueryBuilderBiobank() {
        super(new QueryBiobank());
    }

    @Override
    protected void assignDefaultValues() {
        requiredMissing().forEach(key -> {
            switch (key) {
                case PAGE_CURRENT -> query.setValue(key, PARAMETER_PAGE_DEFAULT_VALUE);
                case PAGE_ITEMS_PER_PAGE -> query.setValue(key, PARAMETER_PER_PAGE_DEFAULT_VALUE);
                default -> {
                }
            }
        });
    }

    @Override
    protected void setPath(String key, String value) {

        if (key.equals(PATH_IDENTITY.getNvaKey())) {
            final var nonNullValue = nonNull(value) ? value : EMPTY_STRING;
            withPathIdentity(nonNullValue);
        } else {
            invalidKeys.add(key);
        }
    }

    @Override
    protected void setValue(String key, String value) {
        final var parameterKey = keyFromString(key, value);
        if (parameterKey.equals(INVALID)) {
            invalidKeys.add(key);
        } else if (parameterKey.equals(LANGUAGE)) {
            logger.info("Ignoring language parameter -> " + value);
        } else {
            query.setValue(parameterKey, value);
        }
    }

    @Override
    protected Set<String> validKeys() {
        return VALID_QUERY_PARAMETER_NVA_KEYS;
    }


    public QueryBuilderBiobank withPathIdentity(String identity) {
        return withPathBiobank(identity);
    }

    public QueryBuilderBiobank withPathBiobank(String biobank) {
        query.setPath(PATH_BIOBANK, biobank);
        return this;
    }
}
