package com.example.departament.Controller;


import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Edificio;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.EdificioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/edificio")
public class EdificioController {
    private final EdificioRepository edificioRepository;
    private final DepartamentoRepository departamentoRepository;

    @Autowired
    public EdificioController(EdificioRepository edificioRepository,
                              DepartamentoRepository departamentoRepository) {
        this.edificioRepository = edificioRepository;
        this.departamentoRepository = departamentoRepository;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Edificio> createEdificio(@RequestBody Edificio edificio) {
        Edificio savedEdificio = edificioRepository.save(edificio);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEdificio);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Edificio>> getAllEdificios() {
        List<Edificio> edificios = edificioRepository.findAll();
        return ResponseEntity.ok(edificios);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Edificio> getEdificioById(@PathVariable Long id) {
        return edificioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Edificio> updateEdificio(@PathVariable Long id, @RequestBody Edificio edificioDetails) {
        return edificioRepository.findById(id)
                .map(edificio -> {
                    edificio.setNombre(edificioDetails.getNombre());
                    edificio.setDireccion(edificioDetails.getDireccion());
                    edificio.setNroPisos(edificioDetails.getNroPisos());
                    edificio.setDescripcion(edificioDetails.getDescripcion());
                    Edificio updatedEdificio = edificioRepository.save(edificio);
                    return ResponseEntity.ok(updatedEdificio);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEdificio(@PathVariable Long id) {
        return edificioRepository.findById(id)
                .map(edificio -> {
                    edificioRepository.delete(edificio);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener apartamentos de un edificio Eliminado
    @GetMapping("/{id}/apartamentos")
    public ResponseEntity<List<Departamento>> getApartamentosByEdificio(@PathVariable Long id) {
        if (!edificioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<Departamento> apartamentos = departamentoRepository.findByEdificioId(id);
        return ResponseEntity.ok(apartamentos);
    }

    @GetMapping("/EdificioSimple")
    public ResponseEntity<List<Map<String, Object>>> getAllEdificiosSimplified() {
        List<EdificioRepository.EdificioProjection> edificios = edificioRepository.findAllEdificiosSimplified();

        List<Map<String, Object>> response = edificios.stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", e.getId());
                    map.put("nombre", e.getNombre());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
