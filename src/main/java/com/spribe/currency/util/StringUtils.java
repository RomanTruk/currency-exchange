package com.spribe.currency.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class StringUtils {
    public static String joinList(Collection<String> items) {
        return String.join(",", items);
    }
}
