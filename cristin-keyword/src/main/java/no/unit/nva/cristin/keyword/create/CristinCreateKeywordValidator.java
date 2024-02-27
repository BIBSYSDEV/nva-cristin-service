package no.unit.nva.cristin.keyword.create;

import static java.util.Objects.isNull;
import java.util.Map;
import no.unit.nva.model.TypedLabel;
import no.unit.nva.validation.Validator;
import nva.commons.apigateway.exceptions.ApiGatewayException;
import nva.commons.apigateway.exceptions.BadRequestException;
import nva.commons.core.StringUtils;

public class CristinCreateKeywordValidator implements Validator<TypedLabel> {

    private static final String NB = "nb";
    private static final String EN = "en";
    public static final String ERROR_MESSAGE =
        "Keyword needs to have a type specified along with descriptive languages in 'nb' and 'en'";

    @Override
    public void validate(TypedLabel input) throws ApiGatewayException {
        if (isPayloadNull(input) || isTypeBlank(input) || isLabelInvalid(input.getLabel())) {
            throw new BadRequestException(ERROR_MESSAGE);
        }
    }

    private boolean isPayloadNull(TypedLabel input) {
        return isNull(input);
    }

    private boolean isTypeBlank(TypedLabel input) {
        return StringUtils.isBlank(input.getType());
    }

    private boolean isLabelInvalid(Map<String, String> label) {
        return isNull(label) || isLabelKeyBlank(label, NB) || isLabelKeyBlank(label, EN);
    }

    private boolean isLabelKeyBlank(Map<String, String> label, String key) {
        return !label.containsKey(key) || label.get(key).isBlank();
    }

}
