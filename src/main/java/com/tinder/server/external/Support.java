package com.tinder.server.external;

import com.tinder.server.model.User;
import java.util.regex.Pattern;

public class Support {

    public static boolean isValidUserInfo(User user) {

        Pattern feildsProfilePattern = Pattern.compile("[A-Za-z0-9_:space:_]");

        if (!user.getGender().isEmpty() && !user.getUsername().isEmpty() && !user.getPassword().isEmpty()) {
            return true;
        } else if (user.getGender().equals("сударь") || user.getGender().equals("сударыня")) {
            return true;
        } else if (feildsProfilePattern.matcher(user.getUsername()).matches()
                && feildsProfilePattern.matcher(user.getPassword()).matches()
                && isValidDescription(user)) {
            return true;
        }
        return false;
    }

    public static boolean isValidDescription(User user) {
        Pattern descriptionPattern = Pattern.compile("[A-Za-z0-9_:space:_]");
        return descriptionPattern.matcher(user.getDescription()).matches();
    }
}
