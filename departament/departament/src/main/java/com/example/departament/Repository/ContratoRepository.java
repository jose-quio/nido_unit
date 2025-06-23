package com.example.departament.Repository;

import com.example.departament.Entity.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    List<Contrato> findByPropietarioId(Long propietarioId);

    List<Contrato> findByDepartamentoId(Long departamentoId);

    List<Contrato> findByTipo(Contrato.TipoContrato tipo);
}
