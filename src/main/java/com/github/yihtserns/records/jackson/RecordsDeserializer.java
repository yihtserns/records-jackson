/*
 * Copyright 2022 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.records.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class RecordsDeserializer<T extends Record> extends StdDeserializer<T> implements ContextualDeserializer {

    private Constructor<T> constructor;

    public RecordsDeserializer() {
        this(Record.class, null);
    }

    private RecordsDeserializer(Class<?> type, Constructor<T> constructor) {
        super(type);
        this.constructor = constructor;
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.isExpectedStartArrayToken()) {
            return (T) context.handleUnexpectedToken(handledType(), parser);
        }

        Parameter[] parameters = constructor.getParameters();
        ArrayNode arrayNode = parser.readValueAsTree();
        if (parameters.length != arrayNode.size()) {
            throw new InvalidFormatException(
                    parser,
                    String.format("Expected JSON array of size %s, but was: %s", parameters.length, arrayNode),
                    arrayNode,
                    handledType());
        }

        List<Object> entries = new ArrayList<>();
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode jsonNode = arrayNode.get(i);
            Parameter parameter = parameters[i];

            entries.add(context.readTreeAsValue(
                    jsonNode,
                    context.getTypeFactory().constructType(parameter.getParameterizedType())));
        }

        try {
            return constructor.newInstance(entries.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        } catch (RuntimeException ex) {
            throw new RuntimeException(
                    String.format("Failed to invoke constructor '%s' using: %s", constructor, entries),
                    ex);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context, BeanProperty property) {
        Class<?> recordType = context.getContextualType().getRawClass();
        Constructor<?>[] constructors = recordType.getConstructors();

        if (!Record.class.isAssignableFrom(recordType)) {
            throw new UnsupportedOperationException(recordType + " is not a record class!");
        }
        if (constructors.length != 1) {
            throw new UnsupportedOperationException(String.format(
                    "Only supporting Records with 1 constructor - %s has %s",
                    recordType,
                    constructors.length));
        }

        return new RecordsDeserializer<T>(recordType, (Constructor) constructors[0]);
    }
}
