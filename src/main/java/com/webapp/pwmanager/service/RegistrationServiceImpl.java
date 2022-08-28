package com.webapp.pwmanager.service;

import com.webapp.pwmanager.domain.AppUser;
import com.webapp.pwmanager.domain.AppUserRole;
import com.webapp.pwmanager.domain.Email;
import com.webapp.pwmanager.util.EmailValidator;
import com.webapp.pwmanager.dto.ConfirmationEmailDto;
import com.webapp.pwmanager.dto.RegistrationDto;
import com.webapp.pwmanager.domain.ConfirmationToken;
import org.springframework.beans.factory.annotation.Value;
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
public class RegistrationServiceImpl implements RegistrationService {
    private final EmailValidator emailValidator;
    private final AppUserService appUserService;

    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    @Value("${email.email_from}")
    private String emailFrom;
    @Value("${email.confirm_link}")
    private String confirmLink;

    public RegistrationServiceImpl(EmailValidator emailValidator, AppUserService appUserService, ConfirmationTokenService confirmationTokenService, EmailSender emailSender) {
        this.emailValidator = emailValidator;
        this.appUserService = appUserService;
        this.confirmationTokenService = confirmationTokenService;
        this.emailSender = emailSender;
    }

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
                ConfirmationToken tokenByAppUserId = confirmationTokenService.getTokenByAppUserId(existentUser.getId())
                        .orElseThrow(() -> new UsernameNotFoundException("User token not found!"));
                HashMap<String, Object> result = new HashMap<>();
                result.put("email", request.getEmail());
                result.put("hasTokenToConfirm", true);
                result.put("resentEmail", true);
                sendEmail(existentUser, tokenByAppUserId.getToken());
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
                AppUserRole.USER.getGrantedAuthorities()
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
        String link = confirmLink + token;
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", user.getEmail());
        properties.put("name", String.format(user.getFirstName() + " " + user.getLastName()));
        properties.put("link", link);
        String subject = "Confirm your email";
        String template = "email-confirmation.html";

        emailSender.send(new Email(properties, user.getEmail(), emailFrom, subject, template));
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
