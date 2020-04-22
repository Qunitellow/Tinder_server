package com.tinder.server.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
@Table(name = "LIKES")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "like_id")
    private Long like_id;
    @Column(name = "like_by")
    private Long like_by;
    @Column(name = "like_to")
    private Long like_to;

    public Like(Long like_by, Long like_to) {
        this.like_by = like_by;
        this.like_to = like_to;
    }
}
