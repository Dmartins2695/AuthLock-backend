package com.webapp.pwmanager.registration;

import com.webapp.pwmanager.appUser.AppUser;
import com.webapp.pwmanager.appUser.AppUserRole;
import com.webapp.pwmanager.appUser.AppUserService;
import com.webapp.pwmanager.config.ConfigVariables;
import com.webapp.pwmanager.email.Email;
import com.webapp.pwmanager.email.EmailSender;
import com.webapp.pwmanager.registration.token.ConfirmationToken;
import com.webapp.pwmanager.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class RegistrationService {
    private final EmailValidator emailValidator;
    private final AppUserService appUserService;

    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;
    private final ConfigVariables configVariables;
    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());
        if(!isValidEmail){
            throw new IllegalStateException(String.format("Email %s not valid!",request.getEmail()));
        }
        String token = appUserService.singUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );

        String link = configVariables.getConfirmLink() + token;
        Map<String, Object> properties = new HashMap<>();
        properties.put("email", request.getEmail());
        properties.put("name", String.format(request.getFirstName() +" "+request.getLastName()));
        properties.put("link", link);
        String subject = "Confirm your email";
        String template = "email-confirmation.html";

        emailSender.send(new Email(properties,request.getEmail(),configVariables.getEmailFrom(),subject,template));

        return token;

    }

    @Transactional
    public String confirmToken(String token) {
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
        return "confirmed";
    }
}
