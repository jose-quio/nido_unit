package com.example.departament.Controller;


import com.example.departament.Entity.Company;
import com.example.departament.Repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company")
public class CompanyController {
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Company> createCompany(@RequestBody Company company) {
        Company savedCompany = companyRepository.save(company);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCompany);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        return ResponseEntity.ok(companies);
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return companyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @RequestBody Company companyDetails) {
        return companyRepository.findById(id)
                .map(company -> {
                    company.setNombre(companyDetails.getNombre());
                    company.setDireccion(companyDetails.getDireccion());
                    company.setTelefono(companyDetails.getTelefono());
                    company.setRuc(companyDetails.getRuc());
                    Company updatedCompany = companyRepository.save(company);
                    return ResponseEntity.ok(updatedCompany);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
