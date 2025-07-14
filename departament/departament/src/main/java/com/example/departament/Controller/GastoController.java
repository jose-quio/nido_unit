package com.example.departament.Controller;

import com.example.departament.Entity.Company;
import com.example.departament.Entity.Contrato;
import com.example.departament.Entity.Gasto;
import com.example.departament.Entity.Pago;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.ContratoRepository;
import com.example.departament.Repository.GastoRepository;
import com.example.departament.Repository.PropietarioRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
public class GastoController {

    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private GastoRepository gastoRepository;
    @Autowired
    private ContratoRepository contratoRepository;

    // CREAR GASTO asociado a una company
    @PostMapping("/company/{companyId}")
    public ResponseEntity<?> crearGasto(@PathVariable Long companyId, @RequestBody Gasto gasto) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);

        if (companyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa no encontrada.");
        }

        gasto.setCompany(companyOpt.get());
        Gasto saved = gastoRepository.save(gasto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // LISTAR GASTOS de una company
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> listarPorCompany(@PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<GastoDTO> gastos = gastoRepository.findByCompanyId(companyId).stream()
                .map(gasto -> new GastoDTO(
                        gasto.getId(),
                        gasto.getDescripcion(),
                        gasto.getMonto(),
                        gasto.getFecha(),
                        gasto.getTipo()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(gastos);
    }

    // ELIMINAR GASTO por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarGasto(@PathVariable Long id) {
        Optional<Gasto> gastoOpt = gastoRepository.findById(id);

        if (gastoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Gasto no encontrado.");
        }

        gastoRepository.delete(gastoOpt.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/company/{companyId}/caja")
    public ResponseEntity<?> getResumenCaja(@PathVariable Long companyId) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);

        if (companyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa no encontrada.");
        }

        Company company = companyOpt.get();

        // 1. Buscar todos los contratos de la empresa
        List<Contrato> contratosEmpresa = contratoRepository.findAll().stream()
                .filter(contrato -> contrato.getDepartamento().getEdificio().getCompany().getId().equals(company.getId()))
                .collect(Collectors.toList());

        // 2. Obtener todos los pagos asociados a esos contratos y filtrar los PAGADOS
        double totalPagos = contratosEmpresa.stream()
                .flatMap(contrato -> contrato.getPagos().stream())
                .filter(p -> p.getEstado() == Pago.EstadoPago.PAGADO)
                .mapToDouble(Pago::getMonto)
                .sum();

        // 3. Sumar todos los gastos de la empresa
        double totalGastos = gastoRepository.findByCompanyId(companyId).stream()
                .mapToDouble(Gasto::getMonto)
                .sum();

        // 4. Calcular utilidad
        double utilidad = totalPagos - totalGastos;

        // 5. Devolver resumen
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalIngresos", totalPagos);
        resumen.put("totalGastos", totalGastos);
        resumen.put("utilidad", utilidad);

        return ResponseEntity.ok(resumen);
    }

    @Getter
    @AllArgsConstructor
    public static class GastoDTO {
        private Long id;
        private String descripcion;
        private Double monto;
        private LocalDate fecha;
        private Gasto.TipoGasto tipo;
    }

}
