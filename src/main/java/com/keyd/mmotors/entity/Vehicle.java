package com.keyd.mmotors.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String energy;

    @Column(nullable = false)
    private Integer mileage;

    private BigDecimal price;

    private BigDecimal monthlyPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleMode mode;

    @Builder.Default
    @Column(nullable = false)
    private Boolean available = true;

    @Column(length = 1000)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
