package com.example.cerpshashkin.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Currency;

@Component
@Slf4j
public final class ResponseConverter {

    private ResponseConverter() {
    }

    public static class CurrencyDeserializer extends JsonDeserializer<Currency> {
        @Override
        public Currency deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            return Currency.getInstance(p.getValueAsString());
        }
    }

    public static class CurrencyKeyDeserializer extends KeyDeserializer {
        @Override
        public Currency deserializeKey(final String key, final DeserializationContext ctxt) {
            return Currency.getInstance(key);
        }
    }

    public static class TimestampToInstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
            return Instant.ofEpochSecond(p.getValueAsLong());
        }
    }
}
