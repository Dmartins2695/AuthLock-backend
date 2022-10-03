package com.webapp.pwmanager.dto;

import com.webapp.pwmanager.domain.Password;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataDTO {
    private Long count;
    private Set<Password> favoritePasswords;

    public DataDTO(Long count) {
        this.count = count;
    }


    public DataDTO(Set<Password> favoritePasswords) {
        this.favoritePasswords = favoritePasswords;
    }
}
