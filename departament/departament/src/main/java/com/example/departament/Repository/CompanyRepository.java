package com.example.departament.Repository;

import com.example.departament.Entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    // Buscar compañía por nombre
    Optional<Company> findByNombre(String nombre);

    // Verificar si existe una compañía por nombre
    boolean existsByNombre(String nombre);

    // Método para contar compañías (opcional)
    long count();
}
