package com.arthurentsev.lynowbom.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/** Ligação pai → filho da estrutura de produto. */
@Entity
@Table(name = "componente",
        uniqueConstraints = @UniqueConstraint(name = "uk_componente_pai_filho", columnNames = {"pai", "filho"}),
        indexes = @Index(name = "ix_componente_pai", columnList = "pai"))
public class ComponenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String pai;

    @Column(nullable = false, length = 60)
    private String filho;

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal quantidade;

    @Column(name = "perda_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal perdaPercentual = BigDecimal.ZERO;

    protected ComponenteEntity() {
    }

    public ComponenteEntity(String pai, String filho, BigDecimal quantidade, BigDecimal perdaPercentual) {
        this.pai = pai;
        this.filho = filho;
        this.quantidade = quantidade;
        this.perdaPercentual = perdaPercentual;
    }

    public Long getId() {
        return id;
    }

    public String getPai() {
        return pai;
    }

    public String getFilho() {
        return filho;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPerdaPercentual() {
        return perdaPercentual;
    }
}
