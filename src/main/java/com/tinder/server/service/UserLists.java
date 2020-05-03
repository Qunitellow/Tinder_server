package com.tinder.server.service;

import com.tinder.server.model.User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserLists {
    public String nameLoggedUser = null;
    protected final Set<User> viewedFemaleProfiles = new HashSet<>();
    protected final Set<User> viewedMaleProfiles = new HashSet<>();
    protected final Set<User> profilesForUnauthorizedUser = new HashSet<>();
    protected final Set<User> alreadyLikeMatchedMaleProfiles = new HashSet<>();
    protected final Set<User> alreadyLikeMatchedFemaleProfiles = new HashSet<>();
    protected final Set<User> alreadyDislikeMatchedMaleProfiles = new HashSet<>();
    protected final Set<User> alreadyDislikeMatchedFemaleProfiles = new HashSet<>();
}
