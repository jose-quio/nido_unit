package com.example.departament.Repository;

import com.example.departament.Entity.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByPropietarioId(Long propietarioId);

    List<Contrato> findByDepartamentoId(Long departamentoId);

    List<Contrato> findByTipo(Contrato.TipoContrato tipo);

    @Query("SELECT c FROM Contrato c WHERE c.departamento.edificio.company.id = :companyId")
    List<Contrato> findByCompanyId(@Param("companyId") Long companyId);

}
