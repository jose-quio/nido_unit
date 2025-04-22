package com.example.departament.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "propietario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Propietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String correo;

    // Relación con Apartamento
    @ManyToMany(mappedBy = "propietarios")
    private Set<Departamento> departamentos;
}
