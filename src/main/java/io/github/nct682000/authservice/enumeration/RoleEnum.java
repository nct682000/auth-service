package io.github.nct682000.authservice.enumeration;

import lombok.Getter;

@Getter
public enum RoleEnum {
    USER("user"),
    ADMIN("admin");

    private final String name;

    RoleEnum(String name) {
        this.name = name;
    }
}
