package com.example.departament.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @ManyToMany(mappedBy = "propietarios",cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties({"propietarios"})
    private Set<Departamento> departamentos = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties({"usuarios", "edificios"})
    private Company company;

}
