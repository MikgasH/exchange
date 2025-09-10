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

/**
 * Unified converter with Jackson deserializers and manual converters.
 */
@Component
@Slf4j
public class ResponseConverter {

    /**
     * Jackson deserializer: String → Currency.
     */
    public static class CurrencyDeserializer extends JsonDeserializer<Currency> {
        @Override
        public Currency deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Currency.getInstance(p.getValueAsString());
        }
    }

    /**
     * Jackson key deserializer: String → Currency for Map keys.
     */
    public static class CurrencyKeyDeserializer extends KeyDeserializer {
        @Override
        public Currency deserializeKey(String key, DeserializationContext ctxt) {
            return Currency.getInstance(key);
        }
    }

    /**
     * Jackson deserializer: UNIX timestamp → Instant.
     */
    public static class TimestampToInstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return Instant.ofEpochSecond(p.getValueAsLong());
        }
    }
}
