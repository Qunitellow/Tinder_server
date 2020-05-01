package com.tinder.server.service;

import com.tinder.server.model.Dislike;
import com.tinder.server.model.User;
import com.tinder.server.repository.DislikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DislikeService {

    private DislikeRepository dislikeRepo;

    @Autowired
    public DislikeService(DislikeRepository dislikeRepo) {
        this.dislikeRepo = dislikeRepo;
    }

    public Iterable<Dislike> get_all() {
        return dislikeRepo.findAll();
    }

    public Dislike getDislikeById(long id) {
        return dislikeRepo.findById(id).orElse(null);
    }

    public void put_one(Dislike dislike) {
        dislikeRepo.save(dislike);
    }

    public void delete_one(long id) {
        dislikeRepo.deleteById(id);
    }

//    public Dislike getUserByName(Long by_id, Long to_id) {
////        return dislikeRepo.relationshipDislikeStatus(by_id, to_id).orElse(null);
////    }
}
