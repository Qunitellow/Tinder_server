package com.tinder.server.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
@Table(name = "DISLIKES")
public class Dislike {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "dislike_id")
    private Long dislike_id;
    @Column(name = "dislike_by")
    private Long dislike_by;
    @Column(name = "dislike_to")
    private Long dislike_to;

    public Dislike(Long dislike_by, Long dislike_to) {
        this.dislike_by = dislike_by;
        this.dislike_to = dislike_to;
    }

}
