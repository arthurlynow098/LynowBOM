package com.arthurentsev.lynowbom.servico;

import com.arthurentsev.lynowbom.core.CalculoOrcamento;
import com.arthurentsev.lynowbom.core.ErroEstrutura;
import com.arthurentsev.lynowbom.core.Explosao;
import com.arthurentsev.lynowbom.dominio.OrcamentoEntity;
import com.arthurentsev.lynowbom.dominio.OrcamentoLinhaEntity;
import com.arthurentsev.lynowbom.dominio.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Service
public class OrcamentoService {

    public record Pedido(
            String produto,
            BigDecimal quantidade,
            String cliente,
            BigDecimal margemPercentual,
            BigDecimal impostosPercentual,
            BigDecimal frete) {
    }

    private final CatalogoService catalogo;
    private final OrcamentoRepository orcamentos;

    public OrcamentoService(CatalogoService catalogo, OrcamentoRepository orcamentos) {
        this.catalogo = catalogo;
        this.orcamentos = orcamentos;
    }

    @Transactional
    public OrcamentoEntity emitir(Pedido pedido) {
        Explosao.Resultado r = catalogo.explodir(pedido.produto(), pedido.quantidade());
        CalculoOrcamento.Parametros p = new CalculoOrcamento.Parametros(
                pedido.margemPercentual(), pedido.impostosPercentual(), pedido.frete());
        CalculoOrcamento.Valores v = CalculoOrcamento.calcular(r, p);
        String descricao = r.arvore().get(0).descricao();

        OrcamentoEntity o = new OrcamentoEntity(
                pedido.cliente() == null ? null : pedido.cliente().trim(),
                r.produto(), descricao, r.quantidadePedida(),
                p.margemPercentual(), p.impostosPercentual(), p.frete(),
                v.custoMateriais(), v.custoProcessos(), v.valorMargem(), v.valorImpostos(),
                v.precoTotal(), v.precoUnitario());
        Stream.concat(r.montagens().stream(), r.materiais().stream()).forEach(l -> o.adicionarLinha(
                new OrcamentoLinhaEntity(l.codigo(), l.descricao(), l.unidade(), l.tipo().name(),
                        l.quantidade(), l.custoUnitario(), l.custoTotal())));
        return orcamentos.save(o);
    }

    @Transactional(readOnly = true)
    public List<OrcamentoEntity> recentes() {
        return orcamentos.findTop50ByOrderByCriadoEmDesc();
    }

    @Transactional(readOnly = true)
    public OrcamentoEntity buscar(Long id) {
        OrcamentoEntity o = orcamentos.findById(id)
                .orElseThrow(() -> new ErroEstrutura("Orçamento " + id + " não encontrado."));
        o.getLinhas().size();
        return o;
    }

    /** Lista de materiais do orçamento em CSV compatível com o Excel em português. */
    @Transactional(readOnly = true)
    public String csv(Long id) {
        OrcamentoEntity o = buscar(id);
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("tipo;codigo;descricao;quantidade;unidade;custo_unitario;custo_total\n");
        for (OrcamentoLinhaEntity l : o.getLinhas()) {
            sb.append(String.join(";", l.getTipo(), l.getCodigo(), aspas(l.getDescricao()),
                    br(l.getQuantidade()), l.getUnidade() == null ? "" : l.getUnidade(),
                    br(l.getCustoUnitario()), br(l.getCustoTotal()))).append('\n');
        }
        sb.append(";;Custo de materiais;;;;").append(br(o.getCustoMateriais())).append('\n');
        sb.append(";;Custo de processos;;;;").append(br(o.getCustoProcessos())).append('\n');
        sb.append(";;Margem;;;;").append(br(o.getValorMargem())).append('\n');
        sb.append(";;Frete;;;;").append(br(o.getFrete())).append('\n');
        sb.append(";;Impostos;;;;").append(br(o.getValorImpostos())).append('\n');
        sb.append(";;Preço total;;;;").append(br(o.getPrecoTotal())).append('\n');
        return sb.toString();
    }

    private static String br(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private static String aspas(String s) {
        if (s == null) {
            return "";
        }
        return s.contains(";") || s.contains("\"") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }
}
