package com.example.departament.Repository;

import com.example.departament.Entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository  extends JpaRepository<Rol,Long> {
    // Buscar rol por nombre del enum
    Optional<Rol> findByNombre(Rol.RolNombre nombre);

    // Verificar si existe un rol por nombre
    boolean existsByNombre(Rol.RolNombre nombre);

    // Método para contar roles (opcional, útil para verificaciones)
    long count();
}
