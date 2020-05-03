package com.tinder.server.controller;

import com.tinder.server.external.Response;
import com.tinder.server.service.SupportService;
import com.tinder.server.service.UserLists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class SupportController {

    protected final SupportService supportService;
    protected final UserLists userLists;

    @Autowired
    public SupportController(SupportService supportService, UserLists userLists) {
        this.supportService = supportService;
        this.userLists = userLists;
    }

    @GetMapping("login/currentuser")
    public ResponseEntity<Map<String, String>> getCurrentUser(@RequestParam(required = false) Integer num) {
        Response response = supportService.responseCurrentUser();
        return response.isStatus() ?
                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/breakuser")
    public ResponseEntity<String> breakUser(@RequestBody String s) {
        userLists.nameLoggedUser = null;
        return new ResponseEntity<>(s, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logOut(@RequestBody String s) {
        userLists.nameLoggedUser = null;
        Response response = new Response(true, "|| Вы вышли из учетной записи ||" + s);
        return new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK);
    }
}
