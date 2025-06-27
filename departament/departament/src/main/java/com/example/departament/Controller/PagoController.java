package com.example.departament.Controller;

import com.example.departament.Entity.Pago;
import com.example.departament.Repository.PagoRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {
    @Autowired
    private PagoRepository pagoRepository;

    @GetMapping
    public List<PagoDTO> listarPagos() {
        return pagoRepository.findAll().stream()
                .map(pago -> new PagoDTO(
                        pago.getId(),
                        pago.getMonto(),
                        pago.getPeriodo().toString(),
                        pago.getEstado().name(),
                        pago.getContrato().getPropietario().getNombres() + " " + pago.getContrato().getPropietario().getApellidos(),
                        pago.getContrato().getDepartamento().getNumero()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/por-estado")
    public ResponseEntity<List<PagoDTO>> listarPorEstado(@RequestParam("estado") Pago.EstadoPago estado) {
        List<PagoDTO> pagos = pagoRepository.findByEstado(estado).stream()
                .map(pago -> new PagoDTO(
                        pago.getId(),
                        pago.getMonto(),
                        pago.getPeriodo().toString(),
                        pago.getEstado().name(),
                        pago.getContrato().getPropietario().getNombres() + " " + pago.getContrato().getPropietario().getApellidos(),
                        pago.getContrato().getDepartamento().getNumero()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pagos);
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<?> pagar(@PathVariable Long id) {
        Optional<Pago> pagoOpt = pagoRepository.findById(id);
        if (pagoOpt.isEmpty()) return ResponseEntity.notFound().build();

        Pago pago = pagoOpt.get();
        pago.setEstado(Pago.EstadoPago.PAGADO);
        pago.setFechaPago(LocalDate.now());
        pagoRepository.save(pago);

        return ResponseEntity.ok("Pago registrado con éxito.");
    }

    @GetMapping("/por-contrato/{contratoId}")
    public ResponseEntity<List<PagoDTO>> listarPorContrato(@PathVariable Long contratoId) {
        List<PagoDTO> pagos = pagoRepository.findByContratoId(contratoId).stream()
                .map(pago -> new PagoDTO(
                        pago.getId(),
                        pago.getMonto(),
                        pago.getPeriodo().toString(),
                        pago.getEstado().name(),
                        pago.getContrato().getPropietario().getNombres() + " " + pago.getContrato().getPropietario().getApellidos(),
                        pago.getContrato().getDepartamento().getNumero()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pagos);
    }

    @Getter
    @AllArgsConstructor
    public static class PagoDTO {
        private Long id;
        private Double monto;
        private String periodo;
        private String estado;
        private String nombrePropietario;
        private String numeroDepartamento;
    }

}
