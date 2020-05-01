package com.tinder.server.repository;

import com.tinder.server.model.Dislike;
import com.tinder.server.model.Like;
import com.tinder.server.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface LikeRepository extends CrudRepository<Like, Long> {
    Set<Like> findAllByByIdAndToId(Long byId, Long toId);
    boolean existsLikeByByIdAndToId(Long byId, Long toId);
}
