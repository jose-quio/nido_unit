package com.example.departament.Repository;

import com.example.departament.Entity.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    // Método para encontrar departamentos disponibles
    List<Departamento> findByDisponibleTrue();

    // Método para encontrar departamentos por ID de edificio
    List<Departamento> findByEdificioId(Long edificioId);
}
