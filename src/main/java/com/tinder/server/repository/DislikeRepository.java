package com.tinder.server.repository;

import com.tinder.server.model.Dislike;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface DislikeRepository extends CrudRepository <Dislike, Long> {
        Set<Dislike> findAllByByIdAndToId(Long byId, Long toId);
        boolean existsDislikeByByIdAndToId(Long byId, Long toId);
}
