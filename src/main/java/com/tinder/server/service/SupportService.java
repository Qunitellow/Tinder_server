package com.tinder.server.service;

import com.tinder.server.external.Response;
import com.tinder.server.model.User;
import com.tinder.server.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SupportService {

    protected final UserLists userLists;
    protected final UsersRepository usersRepo;

    @Autowired
    public SupportService(UserLists userLists, UsersRepository usersRepo) {
        this.userLists = userLists;
        this.usersRepo = usersRepo;
    }

    public Response responseCurrentUser() {
        User currentUser = usersRepo.findByUsername(userLists.nameLoggedUser).orElse(null);
        if (currentUser == null) {
            return new Response(false);
        }
        Map<String, String> currentUserAsMap = new HashMap<>();
        currentUserAsMap.put("id", currentUser.getId() + "");
        currentUserAsMap.put("gender", currentUser.getGender());
        currentUserAsMap.put("username", currentUser.getUsername());
        currentUserAsMap.put("password", currentUser.getPassword());
        currentUserAsMap.put("description", currentUser.getDescription());

        return new Response(true, currentUserAsMap);
    }
}
