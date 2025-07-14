package com.example.departament.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "gastos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;
    private Double monto;
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private TipoGasto tipo;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnoreProperties("gastos")
    private Company company;

    public enum TipoGasto {
        SERVICIO,
        MANTENIMIENTO,
        INFRAESTRUCTURA,
        ADMINISTRATIVO,
        OTRO
    }
}
