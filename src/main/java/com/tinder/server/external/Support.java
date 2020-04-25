package com.tinder.server.external;

import com.tinder.server.model.User;

import java.util.Map;
import java.util.regex.Pattern;

public class Support {

    public static boolean isValidUserInfo(Map<String, String> params) {

        Pattern feildsProfilePattern = Pattern.compile("[A-Za-z0-9_:space:_]");

        if (!params.get("sex").isEmpty() && !params.get("username").isEmpty()
                && !params.get("password").isEmpty() && !params.get("profileMessage").isEmpty()) {
            return true;
        } else if (params.get("sex").equals("сударь") || params.get("sex").equals("сударыня")) {
            return true;
        } else return feildsProfilePattern.matcher(params.get("username")).matches()
                && feildsProfilePattern.matcher(params.get("password")).matches()
                && isValidDescription(params.get("profileMessage"));
    }

    public static boolean isValidDescription(String profileMessage) {
        Pattern descriptionPattern = Pattern.compile("[A-Za-z0-9_:space:_]");
        return descriptionPattern.matcher(profileMessage).matches();
    }
}
