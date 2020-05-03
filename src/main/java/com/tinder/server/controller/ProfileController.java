package com.tinder.server.controller;

import com.tinder.server.external.Response;
import com.tinder.server.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProfileController {

    protected final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> addUser(@RequestBody Map<String, String> params) {
        Response response = profileService.responseReg(params);
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> logInUser(@RequestBody Map<String, String> params) {
        Response response = profileService.responseLogIn(params);
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.CREATED);
    }

    @PostMapping("login/edit/delete")
    public ResponseEntity<String> deleteUser(@RequestBody Long id) {
        Response response = profileService.responseDel(id);
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PutMapping("login/edit")
    public ResponseEntity<Object> changeDescription(@RequestBody String newDesc) {
        Response response = profileService.responseEdit(newDesc);
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.CREATED);
    }
}
