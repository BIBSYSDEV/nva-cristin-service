package no.unit.nva.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class CustomInstantSerializer extends StdSerializer<Instant> {

    private static final long serialVersionUID = -3456457891L;
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendInstant(3).toFormatter();

    public CustomInstantSerializer() {
        this(null);
    }

    public CustomInstantSerializer(Class<Instant> t) {
        super(t);
    }

    @Override
    public void serialize(Instant value, JsonGenerator jsonGenerator, SerializerProvider arg2) throws IOException {
        jsonGenerator.writeString(formatter.format(value));
    }

    public static String addMillisToInstantString(String value) {
        Instant instant = Instant.parse(value);
        return formatter.format(instant);
    }
}



