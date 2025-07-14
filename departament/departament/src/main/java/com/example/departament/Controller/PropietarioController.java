package com.example.departament.Controller;


import com.example.departament.Entity.Company;
import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Propietario;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.PropietarioRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/propietario")
public class PropietarioController {
    @Autowired
    private PropietarioRepository propietarioRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private CompanyRepository companyRepository;

    // CREATE
    @PostMapping("company/{companyId}")
    public ResponseEntity<?> createPropietario(@PathVariable Long companyId, @RequestBody Propietario propietario) {

        // Verificar que la empresa exista
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (companyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La empresa con ID " + companyId + " no existe.");
        }

        Company company = companyOpt.get();
        // Validar que el DNI no exista
        if (propietarioRepository.existsByDniAndCompanyId(propietario.getDni(),companyId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un propietario con el DNI: " + propietario.getDni()+ " en esta empresa");
        }
        propietario.setCompany(company);
        Propietario savedPropietario = propietarioRepository.save(propietario);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPropietario);
    }

    // READ ALL
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getAllPropietarios(@PathVariable Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa no encontrada.");
        }
        List<PropietarioController.PropietarioDTO> propietarioDTOS = propietarioRepository.findByCompanyId(companyId).stream()
                .map(propietario -> new PropietarioController.PropietarioDTO(
                        propietario.getId(),
                        propietario.getNombres(),
                        propietario.getApellidos(),
                        propietario.getDni(),
                        propietario.getTelefono(),
                        propietario.getCorreo()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(propietarioDTOS);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Propietario> getPropietarioById(@PathVariable Long id) {
        return propietarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePropietario(
            @PathVariable Long id,
            @RequestBody Propietario propietarioDetails) {

        return propietarioRepository.findById(id)
                .map(propietario -> {
                    // Obtener el companyId actual del propietario
                    Long companyId = propietario.getCompany().getId();
                    // Validar que el nuevo DNI no esté duplicado en la misma empresa,
                    // excluyendo al mismo propietario
                    boolean dniDuplicado = propietarioRepository
                            .findByDniAndCompanyId(propietarioDetails.getDni(), companyId)
                            .filter(p -> !p.getId().equals(propietario.getId()))
                            .isPresent();

                    if (dniDuplicado) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Ya existe otro propietario con el DNI: " + propietarioDetails.getDni() + " en esta empresa.");
                    }
                    propietario.setNombres(propietarioDetails.getNombres());
                    propietario.setApellidos(propietarioDetails.getApellidos());
                    propietario.setDni(propietarioDetails.getDni());
                    propietario.setTelefono(propietarioDetails.getTelefono());
                    propietario.setCorreo(propietarioDetails.getCorreo());

                    Propietario updatedPropietario = propietarioRepository.save(propietario);
                    return ResponseEntity.ok(updatedPropietario);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePropietario(@PathVariable Long id) {
        return propietarioRepository.findById(id)
                .map(propietario -> {
                    propietarioRepository.delete(propietario);
                    return ResponseEntity.status(HttpStatus.OK).body("El propietario ha sido eliminado");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("El propietario no existe"));
    }

    // Obtener departamentos de un propietario
    @GetMapping("/{id}/departamentos")
    public ResponseEntity<Set<Departamento>> getDepartamentosByPropietario(@PathVariable Long id) {
        return propietarioRepository.findById(id)
                .map(propietario -> ResponseEntity.ok(propietario.getDepartamentos()))
                .orElse(ResponseEntity.notFound().build());
    }

    // Asignar departamento a propietario
    @PostMapping("/{propietarioId}/departamentos/{departamentoId}")
    @Transactional
    public ResponseEntity<?> asignarDepartamentoAPropietario(
            @PathVariable Long propietarioId,
            @PathVariable Long departamentoId) {

        Optional<Propietario> propietarioOpt = propietarioRepository.findById(propietarioId);
        Optional<Departamento> departamentoOpt = departamentoRepository.findById(departamentoId);

        if (propietarioOpt.isEmpty() || departamentoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Propietario propietario = propietarioOpt.get();
        Departamento departamento = departamentoOpt.get();

        // Verificar si la relación ya existe
        if (propietario.getDepartamentos().contains(departamento)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El propietario ya está asignado a este departamento");
        }

        if (departamento.getPropietarios() == null) {
            departamento.setPropietarios(new ArrayList<>());
        }
        if (propietario.getDepartamentos() == null) {
            propietario.setDepartamentos(new HashSet<>());
        }

        // Establecer la relación en ambos lados
        propietario.getDepartamentos().add(departamento);
        departamento.getPropietarios().add(propietario);

        propietarioRepository.save(propietario);
        departamentoRepository.save(departamento);

        return ResponseEntity.ok().build();
    }

    @Getter
    @AllArgsConstructor
    public static class PropietarioDTO {
        private Long id;
        private String nombres;
        private String apellidos;
        private String dni;
        private String telefono;
        private String correo;
    }
}
