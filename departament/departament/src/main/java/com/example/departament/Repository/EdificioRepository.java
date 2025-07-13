package com.example.departament.Repository;

import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Edificio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EdificioRepository extends JpaRepository<Edificio, Long> {
    @Query("SELECT e.id as id, e.nombre as nombre, e.nroPisos as nroPisos FROM Edificio e")
    List<EdificioProjection> findAllEdificiosSimplified();

    List<Edificio> findByCompanyId(Long companyId);

    @Query("SELECT e.nroPisos FROM Edificio e WHERE e.id = :id")
    Integer findNroPisosById(@Param("id") Long id);





    interface EdificioProjection {
        Long getId();
        String getNombre();
        Integer getNroPisos();
    }
}
