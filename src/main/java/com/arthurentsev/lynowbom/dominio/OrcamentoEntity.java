package com.arthurentsev.lynowbom.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Orçamento gravado com uma cópia dos custos do momento da emissão.
 * Alterar o preço de um item depois não muda orçamentos já enviados.
 */
@Entity
@Table(name = "orcamento")
public class OrcamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String cliente;

    @Column(nullable = false, length = 60)
    private String produto;

    @Column(name = "descricao_produto", length = 200)
    private String descricaoProduto;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "margem_percentual", nullable = false, precision = 7, scale = 2)
    private BigDecimal margemPercentual;

    @Column(name = "impostos_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal impostosPercentual;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal frete;

    @Column(name = "custo_materiais", nullable = false, precision = 15, scale = 2)
    private BigDecimal custoMateriais;

    @Column(name = "custo_processos", nullable = false, precision = 15, scale = 2)
    private BigDecimal custoProcessos;

    @Column(name = "valor_margem", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMargem;

    @Column(name = "valor_impostos", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorImpostos;

    @Column(name = "preco_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoTotal;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orcamento_id", nullable = false)
    @OrderColumn(name = "ordem")
    private List<OrcamentoLinhaEntity> linhas = new ArrayList<>();

    protected OrcamentoEntity() {
    }

    public OrcamentoEntity(String cliente, String produto, String descricaoProduto, BigDecimal quantidade,
                           BigDecimal margemPercentual, BigDecimal impostosPercentual, BigDecimal frete,
                           BigDecimal custoMateriais, BigDecimal custoProcessos, BigDecimal valorMargem,
                           BigDecimal valorImpostos, BigDecimal precoTotal, BigDecimal precoUnitario) {
        this.cliente = cliente;
        this.produto = produto;
        this.descricaoProduto = descricaoProduto;
        this.quantidade = quantidade;
        this.margemPercentual = margemPercentual;
        this.impostosPercentual = impostosPercentual;
        this.frete = frete;
        this.custoMateriais = custoMateriais;
        this.custoProcessos = custoProcessos;
        this.valorMargem = valorMargem;
        this.valorImpostos = valorImpostos;
        this.precoTotal = precoTotal;
        this.precoUnitario = precoUnitario;
    }

    public void adicionarLinha(OrcamentoLinhaEntity linha) {
        linhas.add(linha);
    }

    public Long getId() { return id; }
    public String getCliente() { return cliente; }
    public String getProduto() { return produto; }
    public String getDescricaoProduto() { return descricaoProduto; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getMargemPercentual() { return margemPercentual; }
    public BigDecimal getImpostosPercentual() { return impostosPercentual; }
    public BigDecimal getFrete() { return frete; }
    public BigDecimal getCustoMateriais() { return custoMateriais; }
    public BigDecimal getCustoProcessos() { return custoProcessos; }
    public BigDecimal getValorMargem() { return valorMargem; }
    public BigDecimal getValorImpostos() { return valorImpostos; }
    public BigDecimal getPrecoTotal() { return precoTotal; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public Instant getCriadoEm() { return criadoEm; }
    public List<OrcamentoLinhaEntity> getLinhas() { return linhas; }
}
