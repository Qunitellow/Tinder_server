package com.tinder.server.repository;

import com.tinder.server.model.Dislike;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DislikeRepository extends CrudRepository<Dislike, Long> {
    Dislike findDislikeByByIdAndToId(Long byId, Long toId);

    boolean existsDislikeByByIdAndToId(Long byId, Long toId);
}
