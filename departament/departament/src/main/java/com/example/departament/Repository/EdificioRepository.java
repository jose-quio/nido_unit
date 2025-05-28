package com.example.departament.Repository;

import com.example.departament.Entity.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EdificioRepository extends JpaRepository<Edificio, Long> {
    @Query("SELECT e.id as id, e.nombre as nombre FROM Edificio e")
    List<EdificioProjection> findAllEdificiosSimplified();

    List<Edificio> findByCompanyId(Long companyId);

    interface EdificioProjection {
        Long getId();
        String getNombre();
    }
}
