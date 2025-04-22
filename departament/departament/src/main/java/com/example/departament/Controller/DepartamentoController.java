package com.example.departament.Controller;


import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Edificio;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.EdificioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apartamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EdificioRepository edificioRepository;

    // CREATE
    @PostMapping
    public ResponseEntity<Departamento> createApartamento(@RequestBody Departamento apartamento) {
        // Validar que no se envíe una lista de edificios
        if(apartamento.getEdificio() == null || apartamento.getEdificio().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Departamento savedApartamento = departamentoRepository.save(apartamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApartamento);
    }

    // CREATE asociado a edificio
    @PostMapping("/edificio/{edificioId}")
    public ResponseEntity<Departamento> createApartamentoByEdificio(
            @PathVariable Long edificioId,
            @RequestBody Departamento apartamento) {

        return edificioRepository.findById(edificioId)
                .map(edificio -> {
                    apartamento.setEdificio(edificio);
                    Departamento savedApartamento = departamentoRepository.save(apartamento);
                    return ResponseEntity.status(HttpStatus.CREATED).body(savedApartamento);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Departamento>> getAllApartamentos() {
        List<Departamento> apartamentos = departamentoRepository.findAll();
        return ResponseEntity.ok(apartamentos);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Departamento> getApartamentoById(@PathVariable Long id) {
        return departamentoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Departamento> updateApartamento(
            @PathVariable Long id,
            @RequestBody Departamento apartamentoDetails) {

        return departamentoRepository.findById(id)
                .map(apartamento -> {
                    apartamento.setNumero(apartamentoDetails.getNumero());
                    apartamento.setPiso(apartamentoDetails.getPiso());
                    apartamento.setArea(apartamentoDetails.getArea());
                    apartamento.setPrecioVenta(apartamentoDetails.getPrecioVenta());
                    apartamento.setPrecioAlquiler(apartamentoDetails.getPrecioAlquiler());
                    apartamento.setDisponible(apartamentoDetails.getDisponible());

                    Departamento updatedApartamento = departamentoRepository.save(apartamento);
                    return ResponseEntity.ok(updatedApartamento);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartamento(@PathVariable Long id) {
        return departamentoRepository.findById(id)
                .map(apartamento -> {
                    departamentoRepository.delete(apartamento);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener departamentos disponibles
    @GetMapping("/disponibles")
    public ResponseEntity<List<Departamento>> getApartamentosDisponibles() {
        try {
            List<Departamento> apartamentos = departamentoRepository.findByDisponibleTrue();

            if (apartamentos.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204
            }

            return ResponseEntity.ok(apartamentos); // 200
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // 500
        }
    }
}
