package no.unit.nva.cristin.keyword.create;

import static java.util.Objects.isNull;
import java.util.Map;
import no.unit.nva.cristin.keyword.model.nva.Keyword;
import no.unit.nva.validation.Validator;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;

public class CristinCreateKeywordValidator implements Validator<Keyword> {

    private static final String NB = "nb";
    private static final String EN = "en";
    public static final String ERROR_MESSAGE =
        "Keyword needs to have descriptive languages in 'nb' and 'en'";

    @Override
    public void validate(Keyword input) throws ApiGatewayException {
        if (isPayloadNull(input) || isLabelInvalid(input.getLabels())) {
            throw new BadRequestException(ERROR_MESSAGE);
        }
    }

    private boolean isPayloadNull(Keyword input) {
        return isNull(input);
    }

    private boolean isLabelInvalid(Map<String, String> label) {
        return isNull(label) || isLabelKeyBlank(label, NB) || isLabelKeyBlank(label, EN);
    }

    private boolean isLabelKeyBlank(Map<String, String> label, String key) {
        return !label.containsKey(key) || label.get(key).isBlank();
    }

}
