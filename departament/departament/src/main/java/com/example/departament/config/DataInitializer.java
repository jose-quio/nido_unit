package com.example.departament.config;

import com.example.departament.Entity.Company;
import com.example.departament.Entity.Rol;
import com.example.departament.Entity.User;
import com.example.departament.Repository.CompanyRepository;
import com.example.departament.Repository.RolRepository;
import com.example.departament.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RolRepository rolRepository;
    private final CompanyRepository companyRepository;
    //private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           RolRepository rolRepository,
                           CompanyRepository companyRepository
                           /*PasswordEncoder passwordEncoder*/) {
        this.userRepository = userRepository;
        this.rolRepository = rolRepository;
        this.companyRepository = companyRepository;
        //this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        // Crear todos los roles definidos en el enum si no existen
        for (Rol.RolNombre rolNombre : Rol.RolNombre.values()) {
            if (!rolRepository.existsByNombre(rolNombre)) {
                Rol rol = new Rol();
                rol.setNombre(rolNombre);
                rolRepository.save(rol);
                System.out.println("Rol creado: " + rolNombre);
            }
        }
    }

    private void initAdminUser() {
        // Solo crear admin si no hay usuarios
        if (userRepository.count() == 0) {
            // Crear o obtener compañía por defecto
            Company company = companyRepository.findByNombre("Compañía Principal")
                    .orElseGet(() -> {
                        Company newCompany = new Company();
                        newCompany.setNombre("Compañía Principal");
                        return companyRepository.save(newCompany);
                    });

            // Obtener rol ADMIN
            Rol adminRole = rolRepository.findByNombre(Rol.RolNombre.ADMIN_COMPANY)
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN_COMPANY no encontrado"));

            // Crear usuario admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setNombre("Administrador Principal");
            admin.setEmail("admin@compania.com");
            admin.setEnabled(true);
            admin.setCompany(company);
            admin.setRoles(Set.of(adminRole));

            userRepository.save(admin);
            System.out.println("Usuario admin creado: admin/admin123");
        }
    }
}
