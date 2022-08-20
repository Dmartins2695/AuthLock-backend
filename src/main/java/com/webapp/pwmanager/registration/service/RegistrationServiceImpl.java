package com.webapp.pwmanager.registration.service;

import com.webapp.pwmanager.appUser.domain.AppUser;
import com.webapp.pwmanager.appUser.domain.AppUserRole;
import com.webapp.pwmanager.appUser.service.AppUserService;
import com.webapp.pwmanager.config.ConfigVariables;
import com.webapp.pwmanager.email.Email;
import com.webapp.pwmanager.email.EmailSender;
import com.webapp.pwmanager.email.EmailValidator;
import com.webapp.pwmanager.registration.model.ConfirmationEmailDto;
import com.webapp.pwmanager.registration.model.RegistrationDto;
import com.webapp.pwmanager.registration.token.ConfirmationToken;
import com.webapp.pwmanager.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final EmailValidator emailValidator;
    private final AppUserService appUserService;

    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final ConfigVariables configVariables;


    public ResponseEntity<?> register(@Valid RegistrationDto request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if (!isValidEmail) {
            throw new IllegalStateException(String.format("Email %s not valid!", request.getEmail()));
        }

        boolean userExists = appUserService.hasUser(request.getEmail());

        if (userExists) {
            AppUser existentUser = appUserService.loadUserByUsername(request.getEmail());
            boolean hasTokenToConfirm = confirmationTokenService.tokenNotConfirmedButValid(existentUser.getId());
            if (hasTokenToConfirm) {
                HashMap<String, Object> result = new HashMap<>();
                result.put("email", request.getEmail());
                result.put("hasTokenToConfirm", true);
                return ResponseEntity.ok(result);
            }
            appUserService.removeAppUserNotConfirmed(existentUser.getEmail());
            throw new IllegalStateException(String.format("Email %s already exists!", existentUser.getEmail()));
        }

        String token = UUID.randomUUID().toString();

        AppUser newUser = new AppUser(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                AppUserRole.ADMIN.getGrantedAuthorities()
        );

        boolean success = appUserService.singUpUser(newUser
                ,
                token
        );

        sendEmail(newUser, token);

        return success ? ResponseEntity.ok(newUser) : ResponseEntity.noContent().build();

    }

    public ResponseEntity<?> resendConfirmationEmail(@Valid ConfirmationEmailDto request) {
        AppUser existentUser = appUserService.loadUserByUsername(request.getEmail());
        ConfirmationToken confirmToken = confirmationTokenService.getTokenByAppUserId(existentUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Token of user not found"));
        sendEmail(existentUser, confirmToken.getToken());

        return ResponseEntity.ok().build();
    }

    private void sendEmail(AppUser user, String token) {
        String link = configVariables.getConfirmLink() + token;
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", user.getEmail());
        properties.put("name", String.format(user.getFirstName() + " " + user.getLastName()));
        properties.put("link", link);
        String subject = "Confirm your email";
        String template = "email-confirmation.html";

        emailSender.send(new Email(properties, user.getEmail(), configVariables.getEmailFrom(), subject, template));
    }

    @Transactional
    public ResponseEntity<?> confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new IllegalStateException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("token expired");
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(
                confirmationToken.getAppUser().getEmail());
        return ResponseEntity.ok(appUserService.loadUserByUsername(confirmationToken.getAppUser().getEmail()));
    }
}
