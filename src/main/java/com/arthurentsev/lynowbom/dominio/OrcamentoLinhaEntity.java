package com.arthurentsev.lynowbom.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "orcamento_linha")
public class OrcamentoLinhaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String codigo;

    @Column(length = 200)
    private String descricao;

    @Column(length = 12)
    private String unidade;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "custo_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal custoTotal;

    protected OrcamentoLinhaEntity() {
    }

    public OrcamentoLinhaEntity(String codigo, String descricao, String unidade, String tipo,
                                BigDecimal quantidade, BigDecimal custoUnitario, BigDecimal custoTotal) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.unidade = unidade;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.custoTotal = custoTotal;
    }

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public String getUnidade() { return unidade; }
    public String getTipo() { return tipo; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getCustoUnitario() { return custoUnitario; }
    public BigDecimal getCustoTotal() { return custoTotal; }
}
