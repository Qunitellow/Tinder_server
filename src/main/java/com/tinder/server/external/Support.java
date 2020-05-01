package com.tinder.server.external;

import com.tinder.server.model.User;

import java.util.Map;
import java.util.regex.Pattern;

public class Support {

    public static boolean isValidUserInfo(Map<String, String> params) {

        Pattern fieldsProfilePattern = Pattern.compile("([\\w\\sА-Яа-я]+)");

        if (!params.get("gender").isEmpty() && !params.get("username").isEmpty()
                && !params.get("password").isEmpty() && !params.get("description").isEmpty()) {
            return true;
        } else if (params.get("gender").equals("сударь") || params.get("gender").equals("сударыня")) {
            return true;
        } else return fieldsProfilePattern.matcher(params.get("username")).matches()
                && fieldsProfilePattern.matcher(params.get("password")).matches()
                && isValidDescription(params.get("description"));
    }

    public static boolean isValidDescription(String description) {
        Pattern descriptionPattern = Pattern.compile("([\\w\\sА-Яа-я]+)");
        return descriptionPattern.matcher(description).matches();
    }

//    public static void main(String[] args) {
//        String newDesc = "новое описание";
//        boolean res = isValidDescription(newDesc);
//        System.out.println(res);
//    }

}