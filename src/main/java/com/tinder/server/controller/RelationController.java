package com.tinder.server.controller;

import com.tinder.server.external.Response;
import com.tinder.server.model.User;
import com.tinder.server.service.RelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RelationController {

    private static final Logger log = LoggerFactory.getLogger(RelationController.class);
    protected final RelationService relationService;

    @Autowired
    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    @PostMapping("login/dislike")
    public ResponseEntity<Object> putDislike(@RequestBody Long showId) {
        Response response = relationService.responseDislike(showId);
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK);
    }

    @PostMapping("login/like")
    public ResponseEntity<Object> putLike(@RequestBody Long showId) {
        Response response = relationService.responseLike(showId);
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK);
    }

    @GetMapping("me/like/matching/{id}")
    public ResponseEntity<Iterable<User>> matchedUsersByLikes(@PathVariable Long id) {
        Response response = relationService.responseMatch(id);
        return response.isStatus() ?
                new ResponseEntity<>((Iterable<User>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("login/users/next")
    public ResponseEntity<Map<String, String>> nextProfileAuth(@RequestBody Long id) {
        Response response = relationService.responseAuthNext(id);
        return response.isStatus() ?
                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.CREATED);
    }

    @GetMapping("login/users/next")
    public ResponseEntity<Map<String, String>> nextProfileNoAuth(@RequestParam Long num) {
        log.debug("Получение для показа следующего профиля " + num + " с НЕавторизованного юзера...");
        Response response = relationService.responseNoAuthNext();
        return new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK);
    }
}
