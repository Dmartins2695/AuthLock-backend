package com.webapp.pwmanager.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class AppUser implements UserDetails {
    @Id
    @SequenceGenerator(
            name = "app_user_sequence",
            sequenceName = "app_user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_sequence")
    private Long id;
    private String firstName;
    private String LastName;
    private String email;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    private Collection<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER,mappedBy="user")
    @JsonManagedReference
    private Set<Password> passwords = new LinkedHashSet<>();

    private boolean isAccountNonLocked;
    private boolean isEnabled;

    public AppUser(
            String firstName,
            String lastName,
            String email,
            String password, Collection<SimpleGrantedAuthority> grantedAuthorities) {
        this.firstName = firstName;
        LastName = lastName;
        this.email = email;
        this.password = password;
        this.grantedAuthorities = grantedAuthorities;
    }

    public AppUser(
            String firstName,
            String lastName,
            String email,
            String password, Collection<SimpleGrantedAuthority> grantedAuthorities,Boolean isEnabled) {
        this.firstName = firstName;
        LastName = lastName;
        this.email = email;
        this.password = password;
        this.grantedAuthorities = grantedAuthorities;
        this.isEnabled=isEnabled;
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AppUser appUser = (AppUser) o;
        return id != null && Objects.equals(id, appUser.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
