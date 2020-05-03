package com.tinder.server.model;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "gender")
    @NonNull
    private String gender;
    @Column(name = "username")
    @NonNull
    private String username;
    @Column(name = "password")
    @NonNull
    private String password;
    @Column(name = "description")
    @NonNull
    private String description;
}
