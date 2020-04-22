//package com.tinder.server.external;
//
//
//import com.tinder.server.model.User;
//import com.tinder.server.repository.UsersRepository;
//import org.springframework.context.annotation.Configuration;
//
//import java.util.Arrays;
//
//@Configuration
//public class InitUsers {
//
//    private final UsersRepository userRepository;
//
//    public InitUsers(UsersRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public void addUsers() {
//        userRepository.saveAll(Arrays.asList(
//                new User("qwer",  "123"),
//                new User("qwer1", "234"),
//                new User("qwer2", "345"),
//                new User("qwer3",  "456"))
//        );
//    }
//}