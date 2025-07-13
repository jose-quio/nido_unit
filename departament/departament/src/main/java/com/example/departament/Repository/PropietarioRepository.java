package com.example.departament.Repository;

import com.example.departament.Entity.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PropietarioRepository extends JpaRepository<Propietario, Long> {
    // Comprobar si existe un propietario por ID
    boolean existsById(Long id);

    // Comprobar si ya existe un propietario con un DNI dado
    boolean existsByDniAndCompanyId(String dni,Long companyId);

    List<Propietario> findByCompanyId(Long companyId);

    Optional<Propietario> findByDniAndCompanyId(String dni, Long companyId);

    // Comprobar si existe por correo
    boolean existsByCorreo(String correo);

    // Comprobar si existe por teléfono
    boolean existsByTelefono(String telefono);

    // Buscar por DNI (si deseas mostrar datos)
    Optional<Propietario> findByDni(String dni);
}
