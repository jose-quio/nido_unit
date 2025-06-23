package com.example.departament.Entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="contrato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Contrato {

    public enum TipoContrato {
        ALQUILER,
        VENTA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoContrato tipo; // ALQUILER o VENTA

    private Integer cantidadMeses;
    private LocalDate fechaInicio;
    private LocalDate fechaFin; // solo si es ALQUILER
    private Double montoTotal;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
    @JsonIgnoreProperties({"contrato"})
    private Departamento departamento;

    @ManyToOne
    @JoinColumn(name = "propietario_id")
    @JsonIgnoreProperties({"contrato"})
    private Propietario propietario;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"contrato"})
    private List<Pago> pagos = new ArrayList<>();
}
