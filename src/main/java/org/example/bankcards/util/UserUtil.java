package org.example.bankcards.util;

import org.example.bankcards.entity.User.AbstractUser;

public class UserUtil {
    public static String getFullName(AbstractUser user) {
        return user.getFirstName()
                + " " + user.getLastName()
                + " " + user.getMiddleName();
    }
}
