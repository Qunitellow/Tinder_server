package com.tinder.server.model;

import lombok.Data;
import javax.persistence.*;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@RequiredArgsConstructor
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "gender")
//    private UserGender gender;
    private String gender;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
//    @Column(name = "roles")
//    private UserLevel userLvl;
    @Column(name = "description")
    private String description;

    public User(String gender, String username, String password,
                String description) {
        this.gender = gender;
        this.username = username;
        this.password = password;
        this.description = description;
    }

    public User(String username, String description) {
        this.username = username;
        this.description = description;
    }
}
