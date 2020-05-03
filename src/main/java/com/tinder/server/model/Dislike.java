package com.tinder.server.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@RequiredArgsConstructor
@NoArgsConstructor
@Table(name = "DISLIKES")
public class Dislike {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "dislike_id")
    @NonNull
    private Long dislikeId;
    @Column(name = "dislike_by")
    @NonNull
    private Long byId;
    @Column(name = "dislike_to")
    @NonNull
    private Long toId;

    public Dislike(Long byId, Long toId) {
        this.byId = byId;
        this.toId = toId;
    }
}
