package com.kupi.cc.auth.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.kupi.cc.auth.enums.ErrorResponseStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom error body for /oauth/token failures.
 * Renders "user is blocked" when the translator passes message = "BLOCKED".
 * Otherwise renders the standard invalid login text.
 */
@Component
public class CustomInvalidLoginExceptionSerializer extends StdSerializer<CustomLoginException> {

    private transient Translator translator;

    public CustomInvalidLoginExceptionSerializer() {
        super(CustomLoginException.class);
    }

    @Autowired
    public CustomInvalidLoginExceptionSerializer(Translator translator) {
        super(CustomLoginException.class);
        this.translator = translator;
    }

    @Override
    public void serialize(CustomLoginException e,
                          JsonGenerator gen,
                          SerializerProvider serializers) throws IOException {

        String reason = e.getMessage() == null ? "" : e.getMessage().trim();
        boolean isBlocked = "BLOCKED".equalsIgnoreCase(reason);

        gen.writeStartObject();
        gen.writeNumberField("errorCode", ErrorResponseStatusType.INVALID_LOGIN.getCode());
        gen.writeStringField("status", "ERROR");
        gen.writeObjectField("data", null);

        if (isBlocked) {
            gen.writeStringField("message", ErrorResponseStatusType.USER_BLOCKED.getMessage());
            gen.writeStringField("displayMessage", translator.toLocale(
                    ErrorResponseStatusType.USER_BLOCKED.getCodeString(
                            ErrorResponseStatusType.USER_BLOCKED.getCode()
                    )));;
        } else {
            gen.writeStringField("message", ErrorResponseStatusType.INVALID_LOGIN.getMessage());
            gen.writeStringField("displayMessage", translator.toLocale(
                    ErrorResponseStatusType.INVALID_LOGIN.getCodeString(
                            ErrorResponseStatusType.INVALID_LOGIN.getCode()
                    )));
        }

        gen.writeEndObject();
    }
}
