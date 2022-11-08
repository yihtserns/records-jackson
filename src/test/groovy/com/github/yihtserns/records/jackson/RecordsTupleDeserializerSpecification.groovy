package com.github.yihtserns.records.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
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

class RecordsTupleDeserializerSpecification extends Specification {

    private def objectMapper = new ObjectMapper().findAndRegisterModules()

    def "can deserialize JSON array to Record"() {
        given:
        def json = objectMapper.writeValueAsString(jsonValue)

        when:
        def record = objectMapper.readValue(json, recordType)

        then:
        record == expectedRecord

        where:
        recordType           | jsonValue                                                                                | expectedRecord
        SingleInteger        | [1]                                                                                      | new SingleInteger(1)
        SingleIntegerWrapper | [1]                                                                                      | new SingleIntegerWrapper(1)
        SingleObject         | [1]                                                                                      | new SingleObject(1)
        SingleObject         | [1.1]                                                                                    | new SingleObject(1.1d)
        SingleObject         | ["1.1"]                                                                                  | new SingleObject("1.1")
        SingleObject         | [true]                                                                                   | new SingleObject(true)
        SingleEnum           | [JANUARY.name()]                                                                         | new SingleEnum(JANUARY)
        SingleNested         | [[9]]                                                                                    | new SingleNested(new SingleInteger(9))
        SingleList           | [[[1], [2], [3]]]                                                                        | new SingleList([new SingleInteger(1), new SingleInteger(2), new SingleInteger(3)])
        DateTime             | [2022, OCTOBER.name(), 27, THURSDAY.name(), 10, 32, 31.401, -3, 0.5, "America/St_Johns"] | new DateTime(Year.of(2022), OCTOBER, 27, THURSDAY, 10, 32, 31.401d, -3, 0.5f, ZoneId.of("America/St_Johns"))
    }

    def "should throw when trying to deserialize invalid JSON to Record"() {
        given:
        def json = objectMapper.writeValueAsString(invalidValue)

        when:
        objectMapper.readValue(json, recordType)

        then:
        thrown(MismatchedInputException)

        where:
        recordType    | invalidValue
        SingleInteger | [1, 2]
        SingleInteger | []
        SingleInteger | 1
        SingleInteger | true
        SingleInteger | ["a"]
        SingleInteger | [a: 1]
        SingleEnum    | ["NON_EXISTENT"]
    }

    /**
     * Keeping it VERY simple!
     */
    def "only support canonical constructor when there's multiple constructors"() {
        when: "canonical constructor: 1 parameters"
        def record = objectMapper.readValue("[1]", MultipleConstructor)

        then:
        record == new MultipleConstructor(1)

        when: "non-canonical constructor: 2 parameters"
        objectMapper.readValue("[1, 2]", MultipleConstructor)

        then:
        def ex = thrown(InvalidFormatException)
        ex.message.startsWith "Expected JSON array of size 1, but was: [1,2]"
    }

    def "should throw for non-Record class"() {
        when:
        objectMapper.readValue("[1]", NonRecord)

        then:
        def ex = thrown(UnsupportedOperationException)
        ex.message == "${NonRecord} is not a record class!"
    }
}
