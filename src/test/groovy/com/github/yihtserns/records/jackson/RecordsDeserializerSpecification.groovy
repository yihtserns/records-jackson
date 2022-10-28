package com.github.yihtserns.records.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import spock.lang.Specification

import static com.github.yihtserns.records.jackson.TestRecords.MultipleConstructor
import static com.github.yihtserns.records.jackson.TestRecords.NonRecord
import static com.github.yihtserns.records.jackson.TestRecords.SingleEnum
import static com.github.yihtserns.records.jackson.TestRecords.SingleInteger
import static com.github.yihtserns.records.jackson.TestRecords.SingleIntegerWrapper
import static com.github.yihtserns.records.jackson.TestRecords.SingleList
import static com.github.yihtserns.records.jackson.TestRecords.SingleNested
import static com.github.yihtserns.records.jackson.TestRecords.SingleObject
import static java.time.Month.JANUARY

class RecordsDeserializerSpecification extends Specification {

    private def objectMapper = new ObjectMapper()

    def "can deserialize JSON array to Record"() {
        given:
        def json = objectMapper.writeValueAsString(jsonValue)

        when:
        def record = objectMapper.readValue(json, recordType)

        then:
        record == expectedRecord

        where:
        recordType           | jsonValue         | expectedRecord
        SingleInteger        | [1]               | new SingleInteger(1)
        SingleIntegerWrapper | [1]               | new SingleIntegerWrapper(1)
        SingleObject         | [1]               | new SingleObject(1)
        SingleObject         | [1.1]             | new SingleObject(1.1d)
        SingleObject         | ["1.1"]           | new SingleObject("1.1")
        SingleObject         | [true]            | new SingleObject(true)
        SingleEnum           | [JANUARY.name()]  | new SingleEnum(JANUARY)
        SingleNested         | [[9]]             | new SingleNested(new SingleInteger(9))
        SingleList           | [[[1], [2], [3]]] | new SingleList([new SingleInteger(1), new SingleInteger(2), new SingleInteger(3)])
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
    def "does not support Record with multiple constructors"() {
        when:
        objectMapper.readValue("[1]", MultipleConstructor)

        then:
        def ex = thrown(UnsupportedOperationException)
        ex.message == "Only supporting Records with 1 constructor - ${MultipleConstructor} has 2"
    }

    def "should throw for non-Record class"() {
        when:
        objectMapper.readValue("[1]", NonRecord)

        then:
        def ex = thrown(UnsupportedOperationException)
        ex.message == "${NonRecord} is not a record class!"
    }
}
