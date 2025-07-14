package com.example.departament.Repository;

import com.example.departament.Entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago,Long> {
    List<Pago> findByContratoId(Long contratoId);

    List<Pago> findByEstado(Pago.EstadoPago estado);

    List<Pago> findByContratoPropietarioId(Long propietarioId);

    List<Pago> findByPeriodo(YearMonth periodo);

    @Query("SELECT p FROM Pago p WHERE p.estado = 'PENDIENTE' AND p.periodo = :periodo")
    List<Pago> findPagosPendientesPorPeriodo(@Param("periodo") YearMonth periodo);

    @Query("SELECT p FROM Pago p WHERE p.contrato.departamento.edificio.company.id = :companyId")
    List<Pago> findByCompanyId(@Param("companyId") Long companyId);
    @Query("""
    SELECT p FROM Pago p 
    WHERE p.contrato.propietario.dni = :dni 
    AND p.contrato.departamento.edificio.company.id = :companyId
""")
    List<Pago> findByDniAndCompanyId(@Param("dni") String dni, @Param("companyId") Long companyId);

}
