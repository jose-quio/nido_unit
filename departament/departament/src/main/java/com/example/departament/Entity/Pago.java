package com.example.departament.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.*;

@Entity
@Table(name="pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    public enum EstadoPago {
        PENDIENTE,
        PAGADO,
        VENCIDO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double monto;

    private YearMonth periodo; // Representa el mes y año del pago (ej. JUNIO 2025)

    @Enumerated(EnumType.STRING)
    private EstadoPago estado; // PENDIENTE, PAGADO, VENCIDO

    private LocalDate fechaPago; // Solo si ya se pagó

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    @JsonIgnoreProperties({"pagos"})
    private Contrato contrato;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonIgnore
    private Company company;

}
