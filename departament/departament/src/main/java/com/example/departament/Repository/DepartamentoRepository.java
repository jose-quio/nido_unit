package com.example.departament.Repository;

import com.example.departament.Entity.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {
    // Método para encontrar departamentos disponibles
    List<Departamento> findByDisponibleTrue();

    // Método para encontrar departamentos por ID de edificio
    List<Departamento> findByEdificioId(Long edificioId);

    boolean existsByNumeroAndEdificioId(String numero,Long edificioId);

    @Query("SELECT d FROM Departamento d WHERE d.edificio.company.id = :companyId")
    List<Departamento> findByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT d FROM Departamento d JOIN FETCH d.edificio WHERE d.id = :id")
    Optional<Departamento> findByIdWithEdificio(@Param("id") Long id);

    @Query("SELECT d FROM Departamento d WHERE d.disponible = true AND d.edificio.company.id = :companyId")
    List<Departamento> findDisponiblesByCompanyId(@Param("companyId") Long companyId);


}
