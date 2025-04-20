package com.example.departament.Controller;


import com.example.departament.Entity.Departamento;
import com.example.departament.Entity.Edificio;
import com.example.departament.Repository.DepartamentoRepository;
import com.example.departament.Repository.EdificioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apartamentos")
public class DepartamentoController {

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private EdificioRepository edificioRepository;

    // Obtener todos los apartamentos
    @GetMapping
    public List<Departamento> getAllApartamentos() {
        return departamentoRepository.findAll();
    }

    // Obtener apartamento por ID
    @GetMapping("/{id}")
    public Departamento getApartamentoById(@PathVariable Long id) {
        return departamentoRepository.findById(id).orElse(null);
    }

    // Crear nuevo apartamento
    @PostMapping
    public Departamento createApartamento(@RequestBody Departamento apartamento) {
        return departamentoRepository.save(apartamento);
    }

    // Crear apartamento asociado a un edificio específico
    @PostMapping("/edificio/{edificioId}")
    public Departamento createApartamentoByEdificio(@PathVariable Long edificioId, @RequestBody Departamento apartamento) {
        Edificio edificio = edificioRepository.findById(edificioId).orElse(null);
        if (edificio != null) {
            apartamento.setEdificio(edificio);
            return departamentoRepository.save(apartamento);
        } else {
            return null; // Podrías lanzar una excepción personalizada aquí
        }
    }
}
