package com.tinder.server.controller;

import com.tinder.server.model.Dislike;
import com.tinder.server.service.DislikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class DislikeController {

    private final DislikeService service;

    @Autowired
    public DislikeController(DislikeService service) {
        this.service = service;
    }

    @GetMapping("/dislikes")
    public Iterable<Dislike> get_all() {
        return service.get_all();
    }

    @GetMapping("/dislikes/{id}")
    public Dislike get_one(@PathVariable("id") long id) {
        return service.getDislikeById(id);
    }

    @PutMapping("/dislikes")
    public void put_one(@RequestBody Dislike dislike) {
        System.out.println(dislike);
        service.put_one(dislike);
    }

    @DeleteMapping("/dislikes/{id}")
    public void delete_one(@PathVariable("id") long id) {
        service.delete_one(id);
    }
}
