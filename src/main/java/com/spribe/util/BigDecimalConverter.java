package com.spribe.util;

import java.math.BigDecimal;

public class BigDecimalConverter {

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null; // or BigDecimal.ZERO, depending on your requirements
        }
        if (value instanceof Integer) {
            return BigDecimal.valueOf((Integer) value);
        } else if (value instanceof Long) {
            return BigDecimal.valueOf((Long) value);
        } else if (value instanceof Double) {
            return BigDecimal.valueOf((Double) value);
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert value to BigDecimal: " + value, e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported type for BigDecimal conversion: " + value.getClass().getName());
        }
    }
}
