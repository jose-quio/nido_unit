package com.example.departament.Controller;


import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Propietario;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.PropietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/propietario")
public class PropietarioController {
    @Autowired
    private PropietarioRepository propietarioRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;

    // CREATE
    @PostMapping
    public ResponseEntity<Propietario> createPropietario(@RequestBody Propietario propietario) {
        Propietario savedPropietario = propietarioRepository.save(propietario);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPropietario);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Propietario>> getAllPropietarios() {
        List<Propietario> propietarios = propietarioRepository.findAll();
        return ResponseEntity.ok(propietarios);
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
    public ResponseEntity<Propietario> updatePropietario(
            @PathVariable Long id,
            @RequestBody Propietario propietarioDetails) {

        return propietarioRepository.findById(id)
                .map(propietario -> {
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
    public ResponseEntity<Void> deletePropietario(@PathVariable Long id) {
        return propietarioRepository.findById(id)
                .map(propietario -> {
                    propietarioRepository.delete(propietario);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
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

        // Establecer la relación en ambos lados
        propietario.getDepartamentos().add(departamento);
        departamento.getPropietarios().add(propietario);

        propietarioRepository.save(propietario);
        departamentoRepository.save(departamento);

        return ResponseEntity.ok().build();
    }
}
