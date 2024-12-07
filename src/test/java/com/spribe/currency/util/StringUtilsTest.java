package com.spribe.currency.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @ParameterizedTest
    @CsvSource({
            "'USD,EUR,CAD', 'USD,EUR,CAD'",
            "'EUR,USD', 'EUR,USD'",
            "'CAD,USD', 'CAD,USD'",
            "'', ''",
            "'USD', 'USD'",
    })
    void testJoinList(String expected, String input) {
        // given
        Collection<String> items = input == null ? null : List.of(input.split(","));

        // when
        String result = StringUtils.joinList(items);

        // then
        assertEquals(expected, result);
    }
}
