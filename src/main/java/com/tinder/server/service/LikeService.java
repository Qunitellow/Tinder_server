package com.tinder.server.service;

import com.tinder.server.model.Like;
import com.tinder.server.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private LikeRepository likeRepo;

    @Autowired
    public LikeService(LikeRepository likeRepo) {
        this.likeRepo = likeRepo;
    }

    public Iterable<Like> get_all() {
        return likeRepo.findAll();
    }

    public Like getLikeById(Long id) {
        return likeRepo.findById(id).orElse(null);
    }

    public void put_one(Like like) {
        likeRepo.save(like);
    }

    public void delete_one(Long id) {
        likeRepo.deleteById(id);
    }
}
