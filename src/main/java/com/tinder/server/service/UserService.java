package com.tinder.server.service;

import com.tinder.server.model.User;
import com.tinder.server.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class UserService {

    private UsersRepository userRepo;

    @Autowired
    public UserService(UsersRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Iterable<User> get_all() {
        return userRepo.findAll();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public User getUserByName(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    public void put_one(User user) {
        userRepo.save(user);
    }

    public void delete_one(Long id) {
        userRepo.deleteById(id);
    }

    public boolean usernameIsExists(String username) {
        for (User user : get_all()) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public List<User> getUsersAsList(Iterable<User> matchingUsers) {
        List<User> userList = new ArrayList<>();
        matchingUsers.forEach(userList::add);
        return userList;
    }

//    public List<Object> getObjAsList(Iterable<Object> matchingUsers) {
//        List<Object> userList = new ArrayList<>();
//        matchingUsers.forEach(userList::add);
//        return userList;
//    }
}
