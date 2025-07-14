package com.example.departament.Repository;

import com.example.departament.Entity.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto,Long> {
    // Buscar todos los gastos de una empresa por ID
    List<Gasto> findByCompanyId(Long companyId);

    // Comprobar si existe un gasto por ID
    boolean existsById(Long id);
}
