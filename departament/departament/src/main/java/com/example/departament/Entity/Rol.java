package com.example.departament.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    public enum RolNombre {
        ADMIN_COMPANY,
        MANAGER_EDIFICIO,
        ASISTENTE,
        PROPIETARIO,
        INVITADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RolNombre nombre;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnoreProperties({"roles"})
    @Builder.Default
    private Set<User> usuarios = new HashSet<>();



}


