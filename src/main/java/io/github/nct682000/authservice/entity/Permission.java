package io.github.nct682000.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permission")
@Getter
@Setter
public class Permission extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String name;
}
