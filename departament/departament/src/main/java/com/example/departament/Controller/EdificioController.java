package com.example.departament.Controller;


import com.example.departament.Entity.Edificio;
import com.example.departament.Repository.EdificioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edificio")
public class EdificioController {

    @Autowired
    private EdificioRepository edificioRepository;

    @GetMapping
    public List<Edificio> getAllEdificios() {
        return edificioRepository.findAll();
    }

    @PostMapping
    public Edificio createEdificio(@RequestBody Edificio edificio) {
        return edificioRepository.save(edificio);
    }

    @GetMapping("/{id}")
    public Edificio getEdificioById(@PathVariable Long id) {
        return edificioRepository.findById(id).orElse(null);
    }

}
