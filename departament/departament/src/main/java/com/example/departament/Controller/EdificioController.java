package com.example.departament.Controller;


import com.example.departament.Entity.Company;
import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Edificio;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.EdificioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/edificio")
public class EdificioController {
    private final EdificioRepository edificioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public EdificioController(EdificioRepository edificioRepository,
                              DepartamentoRepository departamentoRepository,
                                CompanyRepository companyRepository) {
        this.edificioRepository = edificioRepository;
        this.departamentoRepository = departamentoRepository;
        this.companyRepository = companyRepository;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createEdificio(@RequestBody Edificio edificio) {
        // Verificar que la empresa exista
        Optional<Company> companyOpt = companyRepository.findById(edificio.getCompany().getId());
        if (companyOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("La empresa no existe.");
        }
        // Validar que el DNI no exista
        if (edificioRepository.existsByNombreAndCompanyId(edificio.getNombre(),edificio.getCompany().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un edificio con el nombre: " + edificio.getNombre()+ " en esta empresa");
        }
        Edificio savedEdificio = edificioRepository.save(edificio);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEdificio);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Edificio>> getAllEdificios() {
        List<Edificio> edificios = edificioRepository.findAll();
        return ResponseEntity.ok(edificios);
    }
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Edificio>> getEdificiosByCompanyId(@PathVariable Long companyId) {
        List<Edificio> edificios = edificioRepository.findByCompanyId(companyId);
        return ResponseEntity.ok(edificios);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Edificio> getEdificioById(@PathVariable Long id) {
        return edificioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/nroPisos")
    public ResponseEntity<?> getPisosByEdificio(@PathVariable Long id) {
        if (!edificioRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El edificio no existe");
        }
        Integer nroPisos = edificioRepository.findNroPisosById(id);

        if (nroPisos == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El edificio no tiene asignado la cantidad de pisos");
        }
        return ResponseEntity.ok(Map.of("nroPisos", nroPisos));
    }



    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Edificio> updateEdificio(@PathVariable Long id, @RequestBody Edificio edificioDetails) {
        return edificioRepository.findById(id)
                .map(edificio -> {
                    edificio.setNombre(edificioDetails.getNombre());
                    edificio.setDireccion(edificioDetails.getDireccion());
                    edificio.setNroPisos(edificioDetails.getNroPisos());
                    edificio.setTipo(edificioDetails.getTipo());
                    edificio.setDescripcion(edificioDetails.getDescripcion());

                    Edificio updatedEdificio = edificioRepository.save(edificio);
                    return ResponseEntity.ok(updatedEdificio);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEdificio(@PathVariable Long id) {
        Optional<Edificio> edificioOpt = edificioRepository.findById(id);

        if (edificioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El edificio que quieres eliminar no existe.");
        }

        List<Departamento> departamentos = departamentoRepository.findByEdificioId(id);
        if (!departamentos.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar el edificio porque tiene departamentos asociados.");
        }

        edificioRepository.delete(edificioOpt.get());
        return ResponseEntity.noContent().build();
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
                    map.put("nroPisos", e.getNroPisos());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

}
