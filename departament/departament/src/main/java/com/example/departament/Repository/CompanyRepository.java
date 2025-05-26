package com.example.departament.Repository;

import com.example.departament.Entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
    Optional<Company> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    long count();
}
