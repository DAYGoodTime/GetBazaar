package com.day.getbazzarspring.utils;

import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalDouble;

public class ListUtil {

    public static BigDecimal getListAvg(List<BigDecimal> list) {
        return BigDecimal.valueOf(getDouble(list.stream().mapToDouble(BigDecimal::doubleValue).average()));
    }

    public static BigDecimal getListMax(List<BigDecimal> list) {
        return BigDecimal.valueOf(getDouble(list.stream().mapToDouble(BigDecimal::doubleValue).max()));
    }

    public static BigDecimal getListMin(List<BigDecimal> list) {
        return BigDecimal.valueOf(getDouble(list.stream().mapToDouble(BigDecimal::doubleValue).min()));
    }

    private static double getDouble(OptionalDouble od) {
        if (od.isPresent()) {
            return od.getAsDouble();
        } else {
            return 0;
        }
    }
}
