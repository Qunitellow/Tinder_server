package com.tinder.server.service;

import com.tinder.server.model.User;
import com.tinder.server.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public User getUserById(long id) {
        return userRepo.findById(id).orElse(null);
    }

    public User getUserByName(String username) {
        return userRepo.findByUsername(username).orElse(null);
    }

    public void put_one(User user) {
        user.setPassword(user.getPassword());
        userRepo.save(user);
    }

    public void delete_one(long id) {
        userRepo.deleteById(id);
    }

    public boolean usernameIsExists(User newUser) {
        for (User user : get_all()) {
            if (user.getUsername().equals(newUser.getUsername())) {
                return true;
            }
        }
        return false;
    }

    public List<User> getUsersAsList(Iterable<User> matchingUsers){
        List<User> userList = new LinkedList<>();
        matchingUsers.forEach(userList::add);
        return userList;
    }

}
