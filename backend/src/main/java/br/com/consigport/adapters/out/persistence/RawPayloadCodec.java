package br.com.consigport.adapters.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encoding of the raw payload of the Central in the JSONB column of the trail.
 *
 * <p>The JSONB type of PostgreSQL normalizes the documents it stores: it reorders the
 * keys of an object and it refuses a text that is not JSON. The raw payload of the
 * Central is the evidence of what the Central answered, so it is kept byte for byte:
 * it is stored as a JSON string. The column always holds a valid JSONB and the payload
 * comes back exactly as it was received, whether it is a JSON document or plain text.
 */
final class RawPayloadCodec {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private RawPayloadCodec() {}

  /**
   * Encodes the raw payload as the JSON text of the column.
   *
   * @param rawPayload the payload as the Central answered it, {@code null} when there is
   *     none
   * @return the JSON text of the column, {@code null} when there is no payload
   * @throws IllegalStateException when the payload cannot be encoded
   */
  static String toJsonText(String rawPayload) {
    if (rawPayload == null) {
      return null;
    }
    try {
      return MAPPER.writeValueAsString(rawPayload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("raw payload cannot be encoded as JSON", exception);
    }
  }

  /**
   * Decodes the raw payload from the JSON text of the column.
   *
   * @param jsonText the JSON text of the column, {@code null} when there is no payload
   * @return the payload as the Central answered it
   * @throws IllegalStateException when the column does not hold a JSON string
   */
  static String toRawText(String jsonText) {
    if (jsonText == null) {
      return null;
    }
    try {
      return MAPPER.readValue(jsonText, String.class);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("raw payload cannot be read from JSON", exception);
    }
  }
}
