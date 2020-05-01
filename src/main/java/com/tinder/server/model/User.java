package com.tinder.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import javax.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "gender")
    private String gender;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "description")
    private String description;

    public User(String gender, String username, String password,
                String description) {
        this.gender = gender;
        this.username = username;
        this.password = password;
        this.description = description;
    }
}
