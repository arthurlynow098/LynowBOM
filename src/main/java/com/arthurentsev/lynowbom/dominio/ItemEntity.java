package com.arthurentsev.lynowbom.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "item")
public class ItemEntity {

    @Id
    @Column(length = 60)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String descricao = "";

    @Column(length = 12)
    private String unidade = "";

    @Column(name = "custo_unitario", precision = 15, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    protected ItemEntity() {
    }

    public ItemEntity(String codigo) {
        this.codigo = codigo;
    }

    public void atualizar(String descricao, String unidade, BigDecimal custoUnitario) {
        if (descricao != null && !descricao.isBlank()) {
            this.descricao = descricao.trim();
        }
        if (unidade != null && !unidade.isBlank()) {
            this.unidade = unidade.trim();
        }
        if (custoUnitario != null) {
            this.custoUnitario = custoUnitario;
        }
        this.atualizadoEm = Instant.now();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getCustoUnitario() {
        return custoUnitario;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
