package com.webapp.pwmanager.appUser.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;


@Getter
@Setter
public class PasswordDTO {

    private Long id;

    @Size(max = 255)
    private String value;

    @Size(max = 255)
    private String hash;

    @Size(max = 255)
    private String websiteUrl;

    private Boolean weak;

    private Boolean favorite;

    private Boolean duplicated;

    @NotNull
    private Long userId;

}
