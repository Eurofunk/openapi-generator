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

import java.util.Objects;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Arrays;
import org.openapitools.client.model.Quadrilateral;
import org.openapitools.client.model.Triangle;



import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import org.openapitools.client.JSON;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.6.0-SNAPSHOT")
public class ShapeOrNull extends AbstractOpenApiSchema {
    private static final Logger log = Logger.getLogger(ShapeOrNull.class.getName());

    public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!ShapeOrNull.class.isAssignableFrom(type.getRawType())) {
                return null; // this class only serializes 'ShapeOrNull' and its subtypes
            }
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
            final TypeAdapter<Triangle> adapterTriangle = gson.getDelegateAdapter(this, TypeToken.get(Triangle.class));
            final TypeAdapter<Quadrilateral> adapterQuadrilateral = gson.getDelegateAdapter(this, TypeToken.get(Quadrilateral.class));

            return (TypeAdapter<T>) new TypeAdapter<ShapeOrNull>() {
                @Override
                public void write(JsonWriter out, ShapeOrNull value) throws IOException {
                    if (value == null || value.getActualInstance() == null) {
                        elementAdapter.write(out, null);
                        return;
                    }

                    // check if the actual instance is of the type `Triangle`
                    if (value.getActualInstance() instanceof Triangle) {
                        JsonElement element = adapterTriangle.toJsonTree((Triangle)value.getActualInstance());
                        elementAdapter.write(out, element);
                        return;
                    }
                    // check if the actual instance is of the type `Quadrilateral`
                    if (value.getActualInstance() instanceof Quadrilateral) {
                        JsonElement element = adapterQuadrilateral.toJsonTree((Quadrilateral)value.getActualInstance());
                        elementAdapter.write(out, element);
                        return;
                    }
                    throw new IOException("Failed to serialize as the type doesn't match oneOf schemas: Quadrilateral, Triangle");
                }

                @Override
                public ShapeOrNull read(JsonReader in) throws IOException {
                    Object deserialized = null;
                    JsonElement jsonElement = elementAdapter.read(in);

                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    // use discriminator value for faster oneOf lookup
                    ShapeOrNull newShapeOrNull = new ShapeOrNull();
                    if (jsonObject.get("shapeType") == null) {
                        log.log(Level.WARNING, "Failed to lookup discriminator value for ShapeOrNull as `shapeType` was not found in the payload or the payload is empty.");
                    } else  {
                        // look up the discriminator value in the field `shapeType`
                        switch (jsonObject.get("shapeType").getAsString()) {
                            case "Quadrilateral":
                                deserialized = adapterQuadrilateral.fromJsonTree(jsonObject);
                                newShapeOrNull.setActualInstance(deserialized);
                                return newShapeOrNull;
                            case "Triangle":
                                deserialized = adapterTriangle.fromJsonTree(jsonObject);
                                newShapeOrNull.setActualInstance(deserialized);
                                return newShapeOrNull;
                            default:
                                log.log(Level.WARNING, String.format("Failed to lookup discriminator value `%s` for ShapeOrNull. Possible values: Quadrilateral Triangle", jsonObject.get("shapeType").getAsString()));
                        }
                    }

                    int match = 0;
                    ArrayList<String> errorMessages = new ArrayList<>();
                    TypeAdapter actualAdapter = elementAdapter;

                    // deserialize Triangle
                    try {
                        // validate the JSON object to see if any exception is thrown
                        Triangle.validateJsonElement(jsonElement);
                        actualAdapter = adapterTriangle;
                        match++;
                        log.log(Level.FINER, "Input data matches schema 'Triangle'");
                    } catch (Exception e) {
                        // deserialization failed, continue
                        errorMessages.add(String.format("Deserialization for Triangle failed with `%s`.", e.getMessage()));
                        log.log(Level.FINER, "Input data does not match schema 'Triangle'", e);
                    }
                    // deserialize Quadrilateral
                    try {
                        // validate the JSON object to see if any exception is thrown
                        Quadrilateral.validateJsonElement(jsonElement);
                        actualAdapter = adapterQuadrilateral;
                        match++;
                        log.log(Level.FINER, "Input data matches schema 'Quadrilateral'");
                    } catch (Exception e) {
                        // deserialization failed, continue
                        errorMessages.add(String.format("Deserialization for Quadrilateral failed with `%s`.", e.getMessage()));
                        log.log(Level.FINER, "Input data does not match schema 'Quadrilateral'", e);
                    }

                    if (match == 1) {
                        ShapeOrNull ret = new ShapeOrNull();
                        ret.setActualInstance(actualAdapter.fromJsonTree(jsonElement));
                        return ret;
                    }

                    throw new IOException(String.format("Failed deserialization for ShapeOrNull: %d classes match result, expected 1. Detailed failure message for oneOf schemas: %s. JSON: %s", match, errorMessages, jsonElement.toString()));
                }
            }.nullSafe();
        }
    }

    // store a list of schema names defined in oneOf
    public static final Map<String, Class<?>> schemas = new HashMap<String, Class<?>>();

    public ShapeOrNull() {
        super("oneOf", Boolean.TRUE);
    }

    public ShapeOrNull(Object o) {
        super("oneOf", Boolean.TRUE);
        setActualInstance(o);
    }

    static {
        schemas.put("Triangle", Triangle.class);
        schemas.put("Quadrilateral", Quadrilateral.class);
    }

    @Override
    public Map<String, Class<?>> getSchemas() {
        return ShapeOrNull.schemas;
    }

    /**
     * Set the instance that matches the oneOf child schema, check
     * the instance parameter is valid against the oneOf child schemas:
     * Quadrilateral, Triangle
     *
     * It could be an instance of the 'oneOf' schemas.
     */
    @Override
    public void setActualInstance(Object instance) {
        if (instance == null) {
           super.setActualInstance(instance);
           return;
        }

        if (instance instanceof Triangle) {
            super.setActualInstance(instance);
            return;
        }

        if (instance instanceof Quadrilateral) {
            super.setActualInstance(instance);
            return;
        }

        throw new RuntimeException("Invalid instance type. Must be Quadrilateral, Triangle");
    }

    /**
     * Get the actual instance, which can be the following:
     * Quadrilateral, Triangle
     *
     * @return The actual instance (Quadrilateral, Triangle)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getActualInstance() {
        return super.getActualInstance();
    }

    /**
     * Get the actual instance of `Triangle`. If the actual instance is not `Triangle`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `Triangle`
     * @throws ClassCastException if the instance is not `Triangle`
     */
    public Triangle getTriangle() throws ClassCastException {
        return (Triangle)super.getActualInstance();
    }
    /**
     * Get the actual instance of `Quadrilateral`. If the actual instance is not `Quadrilateral`,
     * the ClassCastException will be thrown.
     *
     * @return The actual instance of `Quadrilateral`
     * @throws ClassCastException if the instance is not `Quadrilateral`
     */
    public Quadrilateral getQuadrilateral() throws ClassCastException {
        return (Quadrilateral)super.getActualInstance();
    }

    /**
     * Validates the JSON Element and throws an exception if issues found
     *
     * @param jsonElement JSON Element
     * @throws IOException if the JSON Element is invalid with respect to ShapeOrNull
     */
    public static void validateJsonElement(JsonElement jsonElement) throws IOException {
        // validate oneOf schemas one by one
        int validCount = 0;
        ArrayList<String> errorMessages = new ArrayList<>();
        // validate the json string with Triangle
        try {
            Triangle.validateJsonElement(jsonElement);
            validCount++;
        } catch (Exception e) {
            errorMessages.add(String.format("Deserialization for Triangle failed with `%s`.", e.getMessage()));
            // continue to the next one
        }
        // validate the json string with Quadrilateral
        try {
            Quadrilateral.validateJsonElement(jsonElement);
            validCount++;
        } catch (Exception e) {
            errorMessages.add(String.format("Deserialization for Quadrilateral failed with `%s`.", e.getMessage()));
            // continue to the next one
        }
        if (validCount != 1) {
            throw new IOException(String.format("The JSON string is invalid for ShapeOrNull with oneOf schemas: Quadrilateral, Triangle. %d class(es) match the result, expected 1. Detailed failure message for oneOf schemas: %s. JSON: %s", validCount, errorMessages, jsonElement.toString()));
        }
    }

    /**
     * Create an instance of ShapeOrNull given an JSON string
     *
     * @param jsonString JSON string
     * @return An instance of ShapeOrNull
     * @throws IOException if the JSON string is invalid with respect to ShapeOrNull
     */
    public static ShapeOrNull fromJson(String jsonString) throws IOException {
        return JSON.getGson().fromJson(jsonString, ShapeOrNull.class);
    }

    /**
     * Convert an instance of ShapeOrNull to an JSON string
     *
     * @return JSON string
     */
    public String toJson() {
        return JSON.getGson().toJson(this);
    }
}

