/*
 * OpenAPI Petstore
 * This spec is mainly for testing Petstore server and contains fake endpoints, models. Please do not use this for any other purpose. Special characters: \" \\
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.openapitools.client.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import org.openapitools.client.model.Pig;
import org.openapitools.client.model.Whale;
import org.openapitools.client.model.Zebra;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import org.openapitools.client.JSON;


import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openapitools.client.JSON;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.10.0-SNAPSHOT")
@JsonDeserialize(using=MammalAnyof.MammalAnyofDeserializer.class)
@JsonSerialize(using = MammalAnyof.MammalAnyofSerializer.class)
public class MammalAnyof extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(MammalAnyof.class.getName());

    public static class MammalAnyofSerializer extends StdSerializer<MammalAnyof> {
        public MammalAnyofSerializer(Class<MammalAnyof> t) {
            super(t);
        }

        public MammalAnyofSerializer() {
            this(null);
        }

        @Override
        public void serialize(MammalAnyof value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeObject(value.getActualInstance());
        }
    }

    public static class MammalAnyofDeserializer extends StdDeserializer<MammalAnyof> {
        public MammalAnyofDeserializer() {
            this(MammalAnyof.class);
        }

        public MammalAnyofDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public MammalAnyof deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            JsonNode tree = jp.readValueAsTree();

            Object deserialized = null;
            Class<?> cls = JSON.getClassForElement(tree, new MammalAnyof().getClass());
            if (cls != null) {
                // When the OAS schema includes a discriminator, use the discriminator value to
                // discriminate the anyOf schemas.
                // Get the discriminator mapping value to get the class.
                deserialized = tree.traverse(jp.getCodec()).readValueAs(cls);
                MammalAnyof ret = new MammalAnyof();
                ret.setActualInstance(deserialized);
                return ret;
            }
            // deserialize Whale
            try {
                deserialized = tree.traverse(jp.getCodec()).readValueAs(Whale.class);
                MammalAnyof ret = new MammalAnyof();
                ret.setActualInstance(deserialized);
                return ret;
            } catch (Exception e) {
                // deserialization failed, continue, log to help debugging
                log.log(Level.FINER, "Input data does not match 'MammalAnyof'", e);
            }

            // deserialize Zebra
            try {
                deserialized = tree.traverse(jp.getCodec()).readValueAs(Zebra.class);
                MammalAnyof ret = new MammalAnyof();
                ret.setActualInstance(deserialized);
                return ret;
            } catch (Exception e) {
                // deserialization failed, continue, log to help debugging
                log.log(Level.FINER, "Input data does not match 'MammalAnyof'", e);
            }

            // deserialize Pig
            try {
                deserialized = tree.traverse(jp.getCodec()).readValueAs(Pig.class);
                MammalAnyof ret = new MammalAnyof();
                ret.setActualInstance(deserialized);
                return ret;
            } catch (Exception e) {
                // deserialization failed, continue, log to help debugging
                log.log(Level.FINER, "Input data does not match 'MammalAnyof'", e);
            }

            throw new IOException(String.format("Failed deserialization for MammalAnyof: no match found"));
        }

        /**
         * Handle deserialization of the 'null' value.
         */
        @Override
        public MammalAnyof getNullValue(DeserializationContext ctxt) throws JsonMappingException {
            throw new JsonMappingException(ctxt.getParser(), "MammalAnyof cannot be null");
        }
    }

    // store a list of schema names defined in anyOf
    public static final Map<String, GenericType<?>> schemas = new HashMap<>();

    public MammalAnyof() {
        super("anyOf", Boolean.FALSE);
    }
  /**
   * A container for additional, undeclared properties.
   * This is a holder for any undeclared properties as specified with
   * the 'additionalProperties' keyword in the OAS document.
   */
  private Map<String, Object> additionalProperties;

  /**
   * Set the additional (undeclared) property with the specified name and value.
   * If the property does not already exist, create it otherwise replace it.
   */
  @JsonAnySetter
  public MammalAnyof putAdditionalProperty(String key, Object value) {
    if (this.additionalProperties == null) {
        this.additionalProperties = new HashMap<>();
    }
    this.additionalProperties.put(key, value);
    return this;
  }

  /**
   * Return the additional (undeclared) property.
   */
  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  /**
   * Return the additional (undeclared) property with the specified name.
   */
  public Object getAdditionalProperty(String key) {
    if (this.additionalProperties == null) {
        return null;
    }
    return this.additionalProperties.get(key);
  }

    /**
     * Return true if this mammal_anyof object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o) && Objects.equals(this.additionalProperties, ((MammalAnyof)o).additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getActualInstance(), isNullable(), getSchemaType(), additionalProperties);
    }
    public MammalAnyof(Whale o) {
        super("anyOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public MammalAnyof(Zebra o) {
        super("anyOf", Boolean.FALSE);
        setActualInstance(o);
    }

    public MammalAnyof(Pig o) {
        super("anyOf", Boolean.FALSE);
        setActualInstance(o);
    }

    static {
        schemas.put("Pig", new GenericType<Pig>() {
        });
        schemas.put("Whale", new GenericType<Whale>() {
        });
        schemas.put("Zebra", new GenericType<Zebra>() {
        });
        JSON.registerDescendants(MammalAnyof.class, Collections.unmodifiableMap(schemas));
        // Initialize and register the discriminator mappings.
        Map<String, Class<?>> mappings = new HashMap<>();
        mappings.put("Pig", Pig.class);
        mappings.put("whale", Whale.class);
        mappings.put("zebra", Zebra.class);
        mappings.put("mammal_anyof", MammalAnyof.class);
        JSON.registerDiscriminator(MammalAnyof.class, "className", mappings);
    }

    @Override
    public Map<String, GenericType<?>> getSchemas() {
        return MammalAnyof.schemas;
    }

    /**
     * Set the instance that matches the anyOf child schema, check
     * the instance parameter is valid against the anyOf child schemas:
     * Pig, Whale, Zebra
     *
     * It could be an instance of the 'anyOf' schemas.
     * The anyOf child schemas may themselves be a composed schema (allOf, anyOf, anyOf).
     */
    @Override
    public void setActualInstance(Object instance) {
        if (JSON.isInstanceOf(Whale.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(Zebra.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        if (JSON.isInstanceOf(Pig.class, instance, new HashSet<>())) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException("Invalid instance type. Must be Pig, Whale, Zebra");
    }

    /**
     * Get the actual instance, which can be the following:
     * Pig, Whale, Zebra
     *
     * @return The actual instance (Pig, Whale, Zebra)
     */
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `Whale`. If the actual instance is not `Whale`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `Whale`
     * @throws ClassCastException if the instance is not `Whale`
     */
    public Whale getWhale() throws ClassCastException {
        return (Whale)super.getActualInstance();
    }

    /**
     * Get the actual instance of `Zebra`. If the actual instance is not `Zebra`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `Zebra`
     * @throws ClassCastException if the instance is not `Zebra`
     */
    public Zebra getZebra() throws ClassCastException {
        return (Zebra)super.getActualInstance();
    }

    /**
     * Get the actual instance of `Pig`. If the actual instance is not `Pig`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `Pig`
     * @throws ClassCastException if the instance is not `Pig`
     */
    public Pig getPig() throws ClassCastException {
        return (Pig)super.getActualInstance();
    }

}

