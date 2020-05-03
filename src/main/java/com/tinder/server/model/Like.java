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
@Table(name = "LIKES")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "like_id")
    @NonNull
    private Long likeId;
    @Column(name = "like_by")
    @NonNull
    private Long byId;
    @Column(name = "like_to")
    @NonNull
    private Long toId;

    public Like(Long byId, Long toId) {
        this.byId = byId;
        this.toId = toId;
    }
}
