package com.example.departament.Entity;


import jakarta.persistence.*;

import java.util.List;

@Entity
public class Edificio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;

    @OneToMany(mappedBy = "edificio")
    private List<Departamento> apartamentos;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<Departamento> getApartamentos() {
        return apartamentos;
    }

    public void setApartamentos(List<Departamento> apartamentos) {
        this.apartamentos = apartamentos;
    }
}
