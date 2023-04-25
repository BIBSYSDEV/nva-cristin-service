package no.unit.nva.biobank.model;

import static no.unit.nva.biobank.model.ParameterKeyBiobank.IGNORE_PATH_PARAMETER_INDEX;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.PATH_BIOBANK;
import static no.unit.nva.biobank.model.ParameterKeyBiobank.PATH_IDENTITY;
import java.util.Map.Entry;
import no.unit.nva.cristin.model.CristinQuery;

public class QueryBiobank extends CristinQuery<ParameterKeyBiobank> {

    public static  QueryBuilderBiobank builder() {
        return new QueryBuilderBiobank();
    }

    @Override
    protected String getNvaPathItem(int pathSize, Entry<ParameterKeyBiobank, String> entry) {
        return entry.getKey().equals(PATH_IDENTITY)
                   ? PATH_BIOBANK.getNvaKey()
                   : entry.getKey().getNvaKey();
    }

    @Override
    protected boolean ignoreNvaPathParameters(Entry<ParameterKeyBiobank, String> entry) {
        return entry.getKey().ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

    @Override
    protected boolean ignorePathParameters(Entry<ParameterKeyBiobank, String> entry) {
        return entry.getKey().ordinal() > IGNORE_PATH_PARAMETER_INDEX;
    }

    @Override
    protected String[] getCristinPath() {
        return new String[]{PATH_BIOBANK.getKey(), getValue(PATH_BIOBANK)};
    }
}
