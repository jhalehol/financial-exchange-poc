package com.yellowpepper.challenge.financial.utils;

public class DataValidation {

    public static boolean numberIsNullOrZero(final Number number) {
        return number == null || (number instanceof Integer ? number.intValue() == 0
                : number instanceof Long ? number.longValue() == 0
                : number instanceof Double  ? number.doubleValue() == 0
                : number instanceof Short   ? number.shortValue() == 0
                : number.floatValue() == 0);
    }
}
