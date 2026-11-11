package com.foodordering.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ValidationTest {
    @Test
    void normalizesEmailAndTrimsName() {
        assertEquals("sam@example.com", Validation.email("  Sam@Example.com "));
        assertEquals("Sam", Validation.requiredText(" Sam ", "Name", 100));
    }

    @Test
    void rejectsMalformedEmailAndInvalidPrice() {
        assertThrows(IllegalArgumentException.class, () -> Validation.email("not-an-email"));
        assertThrows(IllegalArgumentException.class, () -> Validation.positivePrice(new BigDecimal("-0.01")));
        assertThrows(IllegalArgumentException.class, () -> Validation.positivePrice(new BigDecimal("1.234")));
    }

    @Test
    void acceptsPositivePriceWithTwoDecimalPlaces() {
        assertEquals(new BigDecimal("12.50"), Validation.positivePrice(new BigDecimal("12.50")));
    }
}
