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
    private Long likeId;
    @Column(name = "like_by")
    private Long byId;
    @Column(name = "like_to")
    private Long toId;

    public Like(Long byId, Long toId) {
        this.byId = byId;
        this.toId = toId;
    }
}
