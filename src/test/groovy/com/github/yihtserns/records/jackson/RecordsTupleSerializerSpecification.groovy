package com.github.yihtserns.records.jackson

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

import java.time.Year
import java.time.ZoneId

import static TestRecords.DateTime
import static TestRecords.MultipleConstructor
import static TestRecords.NonRecord
import static TestRecords.SingleEnum
import static TestRecords.SingleInteger
import static TestRecords.SingleIntegerWrapper
import static TestRecords.SingleList
import static TestRecords.SingleNested
import static TestRecords.SingleObject
import static java.time.DayOfWeek.THURSDAY
import static java.time.Month.JANUARY
import static java.time.Month.OCTOBER

class RecordsTupleSerializerSpecification extends Specification {

    private def objectMapper = new ObjectMapper().findAndRegisterModules()

    def "can serialize Record to JSON array"() {
        when:
        def json = objectMapper.writeValueAsString(recordValue)

        then:
        json == objectMapper.writeValueAsString(expectedJsonValue)

        where:
        expectedJsonValue                                                                        | recordValue
        [1]                                                                                      | new SingleInteger(1)
        [1]                                                                                      | new SingleIntegerWrapper(1)
        [1]                                                                                      | new SingleObject(1)
        [1.1]                                                                                    | new SingleObject(1.1d)
        ["1.1"]                                                                                  | new SingleObject("1.1")
        [true]                                                                                   | new SingleObject(true)
        [JANUARY.name()]                                                                         | new SingleEnum(JANUARY)
        [[9]]                                                                                    | new SingleNested(new SingleInteger(9))
        [[[1], [2], [3]]]                                                                        | new SingleList([new SingleInteger(1), new SingleInteger(2), new SingleInteger(3)])
        [2022, OCTOBER.name(), 27, THURSDAY.name(), 10, 32, 31.401, -3, 0.5, "America/St_Johns"] | new DateTime(Year.of(2022), OCTOBER, 27, THURSDAY, 10, 32, 31.401d, -3, 0.5f, ZoneId.of("America/St_Johns"))
        [1]                                                                                      | new MultipleConstructor(1)
        [3]                                                                                      | new MultipleConstructor(1, 2)
    }

    def "should throw for non-Record class"() {
        when:
        objectMapper.writeValueAsString(new NonRecord())

        then:
        def ex = thrown(JsonMappingException)
        ex.cause instanceof ClassCastException
        ex.cause.message.startsWith "${NonRecord} cannot be cast to class java.lang.Record"
    }
}
