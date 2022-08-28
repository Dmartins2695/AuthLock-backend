package com.webapp.pwmanager.domain;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum AppUserRole {
    USER(Sets.newHashSet()),
    ADMIN(Sets.newHashSet(AppUserPermission.PASSWORD_READ, AppUserPermission.PASSWORD_WRITE));

    private final Set<AppUserPermission> permissions;

    public Collection<SimpleGrantedAuthority> getGrantedAuthorities() {
        Collection<SimpleGrantedAuthority> permissions = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toSet());
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return permissions;
    }
}
