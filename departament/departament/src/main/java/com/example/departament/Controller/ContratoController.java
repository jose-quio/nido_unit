package com.example.departament.Controller;

import com.example.departament.Entity.*;
import com.example.departament.Repository.*;
import com.example.departament.Service.CorreoService;
import com.example.departament.Service.PdfService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contratos")
public class ContratoController {
    @Autowired
    private  ContratoRepository contratoRepository;
    @Autowired
    private  DepartamentoRepository departamentoRepository;
    @Autowired
    private  PropietarioRepository propietarioRepository;
    @Autowired
    private  PagoRepository pagoRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private PdfService pdfService;

    @Autowired
    private CorreoService correoService;


    @PostMapping("/company/{companyId}")
    public ResponseEntity<?> crearContrato(@PathVariable Long companyId,@RequestBody ContratoRequestDTO dto) {

        // Validación de existencia de IDs
        if (dto.getDepartamentoId() == null || dto.getPropietarioId() == null) {
            return ResponseEntity.badRequest().body("Debe proporcionar IDs válidos para departamento y propietario.");
        }

        Optional<Departamento> depOpt = departamentoRepository.findByIdWithEdificio(dto.getDepartamentoId());
        Optional<Propietario> propOpt = propietarioRepository.findById(dto.getPropietarioId());

        if (depOpt.isEmpty() || propOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Departamento o Propietario no encontrado.");
        }

        Departamento departamento = depOpt.get();
        Propietario propietario = propOpt.get();

        // Validar que el departamento pertenece a la empresa
        if (!departamento.getEdificio().getCompany().getId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El departamento no pertenece a la empresa con ID " + companyId);
        }
        if (!departamentoRepository.existsByIdAndDisponibleTrue(dto.departamentoId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El departamento no esta disponible para un contrato ");
        }

        // Validar que el propietario pertenece a la misma empresa
        if (!propietario.getCompany().getId().equals(companyId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El propietario no pertenece a la empresa con ID " + companyId);
        }

        Contrato contrato = new Contrato();
        contrato.setTipo(dto.getTipo());
        contrato.setFechaInicio(dto.getFechaInicio());
        contrato.setDepartamento(departamento);
        contrato.setPropietario(propietario);

        if (dto.getTipo() == Contrato.TipoContrato.ALQUILER) {

            // Validación para cantidadMeses
            if (dto.getCantidadMeses() == null || dto.getCantidadMeses() < 1) {
                return ResponseEntity.badRequest().body("La cantidad de meses debe ser mayor a 0 para contratos de ALQUILER.");
            }

            // Calcular fechaFin
            LocalDate fechaFin = dto.getFechaInicio().plusMonths(dto.getCantidadMeses() - 1);
            contrato.setFechaFin(fechaFin);
            contrato.setCantidadMeses(dto.getCantidadMeses());

            // Calcular monto total
            contrato.setMontoTotal(departamento.getPrecioAlquiler() * dto.getCantidadMeses());

        } else if (dto.getTipo() == Contrato.TipoContrato.VENTA) {
            contrato.setFechaFin(dto.getFechaInicio());
            contrato.setMontoTotal(departamento.getPrecioVenta());
            contrato.setCantidadMeses(1); // opcional, solo informativo
        }

        // Guardar contrato primero
        Contrato contratoGuardado = contratoRepository.save(contrato);
        departamento.setDisponible(false);
        departamentoRepository.save(departamento);




        Company company = departamento.getEdificio().getCompany();
        // Generar pagos
        if (dto.getTipo() == Contrato.TipoContrato.ALQUILER) {
            List<Pago> pagos = new ArrayList<>();
            LocalDate fecha = dto.getFechaInicio();
            for (int i = 0; i < dto.getCantidadMeses(); i++) {
                pagos.add(Pago.builder()
                        .contrato(contratoGuardado)
                        .monto(departamento.getPrecioAlquiler())
                        .periodo(YearMonth.from(fecha))
                        .estado(Pago.EstadoPago.PENDIENTE)
                        .company(company)
                        .build()
                );
                fecha = fecha.plusMonths(1);
            }
            pagoRepository.saveAll(pagos);

        } else if (dto.getTipo() == Contrato.TipoContrato.VENTA) {
            List<Pago> pagos = new ArrayList<>();
            LocalDate fecha = dto.getFechaInicio();
            Double montoVentaDividido = departamento.getPrecioVenta()/dto.getCantidadMeses();
            for (int i = 0; i < dto.getCantidadMeses(); i++) {
                pagos.add(Pago.builder()
                        .contrato(contratoGuardado)
                        .monto(montoVentaDividido)
                        .periodo(YearMonth.from(fecha))
                        .estado(Pago.EstadoPago.PENDIENTE)
                        .company(company)
                        .build()
                );
                fecha = fecha.plusMonths(1);
            }
            pagoRepository.saveAll(pagos);
        }

        return ResponseEntity.ok(contratoGuardado);
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> listarContratos(@PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa no encontrada.");
        }
        List<Contrato> contratos = contratoRepository.findByCompanyId(companyId);

        List<ContratoResponseDTO> contratosDTO = contratos.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(contratosDTO);
    }

    @Getter
    @Setter
    public static class ContratoRequestDTO {

        private Contrato.TipoContrato tipo;
        private LocalDate fechaInicio;
        private Integer cantidadMeses;
        private Long departamentoId;
        private Long propietarioId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContratoResponseDTO {
        private Long idContrato;
        private String nombrePropietario;
        private String numeroDepartamento;
        private Double montoTotal;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private String tipo;
    }

    private ContratoResponseDTO mapToDTO(Contrato contrato) {
        return new ContratoResponseDTO(
                contrato.getId(),
                contrato.getPropietario().getNombres() + " " + contrato.getPropietario().getApellidos(),
                contrato.getDepartamento().getNumero(),
                contrato.getMontoTotal(),
                contrato.getFechaInicio(),
                contrato.getFechaFin(),
                contrato.getTipo().toString()
        );
    }
}
