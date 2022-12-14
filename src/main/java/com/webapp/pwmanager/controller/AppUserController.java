package com.webapp.pwmanager.controller;

import com.webapp.pwmanager.dto.UpdateDataDto;
import com.webapp.pwmanager.service.AppUserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/user")
@AllArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;


    //TODO: change to a new controller
    @GetMapping("/stored-passwords/{userId}")
    public ResponseEntity<?> getUserStoredPasswords(@Valid @PathVariable Long userId) {
        return appUserService.getUserStoredPasswords(userId);
    }

    @GetMapping("/stored-passwords/weak//{userId}")
    public ResponseEntity<?> countWeakPasswords(@Valid @PathVariable Long userId) {

        return appUserService.countWeakPasswords(userId);
    }

    @GetMapping("/stored-passwords/outdated/{userId}")
    public ResponseEntity<?> countOutdatedPasswords(@Valid @PathVariable Long userId) {
        return appUserService.countOutdatedPasswords(userId);
    }

    @GetMapping("/stored-passwords/duplicated/{userId}")
    public ResponseEntity<?> countDuplicatedPasswords(@Valid @PathVariable Long userId) {
        return appUserService.countDuplicatedPasswords(userId);
    }

    @GetMapping("/stored-passwords/favorites/{userId}")
    public ResponseEntity<?> findAllFavoritePasswords(@Valid @PathVariable Long userId) {
        return appUserService.findAllFavoritePasswords(userId);
    }

    @PatchMapping("{userId}/{id}/update")
    public ResponseEntity<?> updatePassword(@Valid @PathVariable Long userId, @PathVariable Long id, @Valid @RequestBody UpdateDataDto data) {
        return appUserService.updateUserPassword(userId,id,data);
    }

    @PostMapping("{userId}/create")
    public ResponseEntity<?> createPassword(@Valid @PathVariable Long userId, @Valid @RequestBody UpdateDataDto data) {
        return appUserService.createUserPassword(userId,data);
    }

    @PatchMapping("{userId}/{id}/favorite")
    public ResponseEntity<?> setFavorite(@Valid @PathVariable Long userId, @PathVariable Long id) {
        return appUserService.setFavorite(userId,id);
    }

    @DeleteMapping("{userId}/{id}/delete")
    public ResponseEntity<?> deletePassword(@Valid @PathVariable Long userId, @PathVariable Long id) {
        return appUserService.deletePassword(userId,id);
    }
}
