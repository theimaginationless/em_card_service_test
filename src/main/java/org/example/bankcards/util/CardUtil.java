package org.example.bankcards.util;

import org.apache.commons.lang3.StringUtils;

public class CardUtil {

    public static String maskCardNumber(String cardNumber) {
        if (StringUtils.isEmpty(cardNumber) || cardNumber.length() < 16) {
            throw new IllegalArgumentException("Invalid card number");
        }

        int length = cardNumber.length();
        return "**** **** **** " + cardNumber.substring(length - 4);
    }
}
