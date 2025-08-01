package com.example.departament.Controller;


import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Edificio;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.EdificioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/apartamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EdificioRepository edificioRepository;

    @Autowired
    private CompanyRepository companyRepository;

    // CREATE
    @PostMapping
    public ResponseEntity<?> createApartamento(@RequestBody Departamento apartamento) {
        // Validar que no se envíe una lista de edificios
        if(apartamento.getEdificio() == null || apartamento.getEdificio().getId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ocurrio un error con el edificio seleccionado");
        }
        //validart el numero de departamento
        boolean existeDuplicado = departamentoRepository.existsByNumeroAndEdificioId(apartamento.getNumero(),apartamento.getEdificio().getId());

        if(existeDuplicado){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un departamento con el número '" + apartamento.getNumero() + "' en este edificio.");
        }

        Departamento savedApartamento = departamentoRepository.save(apartamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApartamento);
    }

    // CREATE asociado a edificio
    @PostMapping("company/{companyId}/edificio/{edificioId}")
    public ResponseEntity<?> createApartamentoByEdificio(
            @PathVariable Long companyId,
            @PathVariable Long edificioId,
            @RequestBody Departamento apartamento) {

        Optional<Edificio> edificioOpt = edificioRepository.findById(edificioId);

        if(edificioOpt.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El edificio no existe");
        }

        //validart el numero de departamento
        boolean existeDuplicado = departamentoRepository.existsByNumeroAndEdificioId(apartamento.getNumero(),edificioId);

        if(existeDuplicado){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un departamento con el número '" + apartamento.getNumero() + "' en este edificio.");
        }
        apartamento.setEdificio(edificioOpt.get());
        Departamento savedApartamento = departamentoRepository.save(apartamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApartamento);

    }

    // READ ALL
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getAllApartamentos(@PathVariable Long companyId) {
        if(!companyRepository.existsById(companyId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa no encontrada.");
        }
        List<Departamento> apartamentos = departamentoRepository.findByCompanyId(companyId);
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
                    apartamento.setNroHabitaciones(apartamentoDetails.getNroHabitaciones());
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
    @GetMapping("/disponibles/company/{companyId}")
    public ResponseEntity<?> getApartamentosDisponibles(@PathVariable Long companyId) {
        if(!companyRepository.existsById(companyId)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empresa no encontrada.");
        }
        try {
            List<Departamento> apartamentos = departamentoRepository.findDisponiblesByCompanyId(companyId);

            if (apartamentos.isEmpty()) {
                return ResponseEntity.noContent().build(); // 204
            }

            return ResponseEntity.ok(apartamentos); // 200
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // 500
        }
    }

    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<?> cambiarDisponibilidad(
            @PathVariable Long id,
            @RequestParam boolean disponible) {

        Optional<Departamento> depOpt = departamentoRepository.findById(id);

        if (depOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Departamento no encontrado.");
        }

        Departamento departamento = depOpt.get();
        departamento.setDisponible(disponible);
        departamentoRepository.save(departamento);

        return ResponseEntity.ok("Estado actualizado a: " + (disponible ? "Disponible" : "Ocupado"));
    }

}
