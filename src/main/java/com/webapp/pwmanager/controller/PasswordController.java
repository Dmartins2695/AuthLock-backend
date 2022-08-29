package com.webapp.pwmanager.controller;

import com.webapp.pwmanager.dto.PasswordDTO;
import com.webapp.pwmanager.service.PasswordService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(value = "/api/v1/password", produces = MediaType.APPLICATION_JSON_VALUE)
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(final PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping
    public ResponseEntity<List<PasswordDTO>> getAllPasswords() {
        return ResponseEntity.ok(passwordService.findAll());
    }

    @GetMapping("/weak")
    public ResponseEntity<?> countWeakPasswords() {
        return (ResponseEntity<?>) passwordService.countWeakPasswords();
    }

    @GetMapping("/outdated")
    public ResponseEntity<?> countOutdatedPasswords() {
        return (ResponseEntity<?>) passwordService.countOutdatedPasswords();
    }
    @GetMapping("/duplicated")
    public ResponseEntity<?> countDuplicatedPasswords() {
        return (ResponseEntity<?>) passwordService.countDuplicatedPasswords();
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> findAllFavoritePasswords() {
        return (ResponseEntity<?>) passwordService.findAllFavoritePasswords();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PasswordDTO> getPassword(@PathVariable final Long id) {
        return ResponseEntity.ok(passwordService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createPassword(@RequestBody @Valid final PasswordDTO passwordDTO) {
        return new ResponseEntity<>(passwordService.create(passwordDTO), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePassword(@PathVariable final Long id,
            @RequestBody @Valid final PasswordDTO passwordDTO) {
        passwordService.update(id, passwordDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deletePassword(@PathVariable final Long id) {
        passwordService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
