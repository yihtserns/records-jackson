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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public class RecordsSerializer extends StdSerializer<Record> {

    protected RecordsSerializer() {
        super(Record.class);
    }

    @Override
    public void serialize(Record record, JsonGenerator generator, SerializerProvider provider) throws IOException {
        Class<? extends Record> recordType = record.getClass();
        Constructor<?>[] constructors = recordType.getConstructors();
        if (constructors.length != 1) {
            throw new UnsupportedOperationException(String.format(
                    "Only supporting Records with 1 constructor - %s has %s",
                    recordType,
                    constructors.length));
        }

        try {
            List<Object> entries = new ArrayList<>();
            for (RecordComponent recordComponent : recordType.getRecordComponents()) {
                entries.add(recordComponent.getAccessor().invoke(record));
            }

            JsonSerializer<Object> listSerializer = provider.findValueSerializer(List.class);
            listSerializer.serialize(entries, generator, provider);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            // Should not happen
            throw new RuntimeException(ex);
        }
    }
}
