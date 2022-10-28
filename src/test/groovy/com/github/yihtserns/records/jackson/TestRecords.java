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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;

public class TestRecords {

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record SingleInteger(int value) {
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record SingleIntegerWrapper(Integer value) {
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record SingleObject(Object value) {
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record SingleEnum(Month value) {
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record SingleNested(SingleInteger value) {
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record SingleList(List<SingleInteger> values) {
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record DateTime(Year year,
                           Month month,
                           int day,
                           DayOfWeek dayOfWeek,
                           int hour,
                           int minute,
                           double seconds,
                           int milliseconds,
                           float nanoseconds,
                           ZoneId timeZone) {
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public record MultipleConstructor(int value) {

        public MultipleConstructor(int value, int value2) {
            this(value + value2);
        }
    }

    @JsonDeserialize(using = RecordsDeserializer.class)
    @JsonSerialize(using = RecordsSerializer.class)
    public static class NonRecord {
    }
}
