package com.webapp.pwmanager.appUser.domain;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.webapp.pwmanager.appUser.domain.AppUserPermission.*;

@AllArgsConstructor
@Getter
public enum AppUserRole {
    USER(Sets.newHashSet()),
    ADMIN(Sets.newHashSet(PASSWORD_READ, PASSWORD_WRITE));

    private final Set<AppUserPermission> permissions;

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> permissions = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toSet());
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return permissions;
    }
}
