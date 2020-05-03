package com.tinder.server.repository;

import com.tinder.server.model.Like;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends CrudRepository<Like, Long> {
    Like findLikeByByIdAndToId(Long byId, Long toId);

    boolean existsLikeByByIdAndToId(Long byId, Long toId);
}
