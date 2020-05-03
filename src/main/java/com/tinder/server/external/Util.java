package com.tinder.server.external;

import com.tinder.server.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class Util {

    public static boolean isValidUserInfo(Map<String, String> params) {

        Pattern fieldsProfilePattern = Pattern.compile("([\\w\\sА-Яа-я]+)");

        if (params.get("gender").isEmpty() || params.get("username").isEmpty()
                || params.get("password").isEmpty() || params.get("description").isEmpty()) {
            return false;
        } else if (!params.get("gender").equals("сударь") && !params.get("gender").equals("сударыня")) {
            return false;
        } else return fieldsProfilePattern.matcher(params.get("username")).matches()
                && fieldsProfilePattern.matcher(params.get("password")).matches()
                && isValidDescription(params.get("description"));
    }

    public static boolean isValidDescription(String description) {
        Pattern descriptionPattern = Pattern.compile("([\\w\\sА-Яа-я]+)");
        return descriptionPattern.matcher(description).matches();
    }

    public List<User> getUsersAsList(Iterable<User> matchingUsers) {
        List<User> userList = new ArrayList<>();
        matchingUsers.forEach(userList::add);
        return userList;
    }
}
