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
    private Long dislikeId;
    @Column(name = "dislike_by")
    private Long byId;
    @Column(name = "dislike_to")
    private Long toId;

    public Dislike(Long byId, Long toId) {
        this.byId = byId;
        this.toId = toId;
    }
}
