package com.webapp.pwmanager.appUser.web;

import com.webapp.pwmanager.appUser.model.PasswordDTO;
import com.webapp.pwmanager.appUser.service.PasswordService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping(value = "/api/passwords", produces = MediaType.APPLICATION_JSON_VALUE)
public class PasswordResource {

    private final PasswordService passwordService;

    public PasswordResource(final PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @GetMapping
    public ResponseEntity<List<PasswordDTO>> getAllPasswords() {
        return ResponseEntity.ok(passwordService.findAll());
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
