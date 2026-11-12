package com.arthurentsev.lynowbom.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Explode uma estrutura multinível em lista de materiais consolidada.
 *
 * <p>A quantidade de cada componente é multiplicada pela quantidade do pai e acrescida
 * da perda informada na ligação: {@code qtd × (1 + perda / 100)}. Materiais repetidos
 * em montagens diferentes são somados em uma única linha.</p>
 */
public final class Explosao {

    public static final int ESCALA = 6;

    public enum Tipo { MATERIAL, MONTAGEM }

    public record LinhaMaterial(
            String codigo,
            String descricao,
            String unidade,
            Tipo tipo,
            int nivel,
            BigDecimal quantidade,
            BigDecimal custoUnitario,
            BigDecimal custoTotal) {
    }

    /** Linha da árvore indentada, útil para conferência visual da estrutura. */
    public record NoArvore(String codigo, String descricao, int nivel, BigDecimal quantidadeAcumulada, Tipo tipo) {
    }

    public record Resultado(
            String produto,
            BigDecimal quantidadePedida,
            List<LinhaMaterial> materiais,
            List<LinhaMaterial> montagens,
            List<NoArvore> arvore,
            List<String> avisos,
            BigDecimal custoMateriais,
            BigDecimal custoProcessos) {

        public BigDecimal custoTotal() {
            return custoMateriais.add(custoProcessos);
        }
    }

    private Explosao() {
    }

    public static Resultado explodir(Estrutura estrutura, String codigoProduto, BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new ErroEstrutura("A quantidade do pedido deve ser maior que zero.");
        }
        Estrutura.Item raiz = estrutura.item(codigoProduto)
                .orElseThrow(() -> new ErroEstrutura("Produto " + codigoProduto + " não encontrado."));

        Map<String, Acumulado> acumulados = new LinkedHashMap<>();
        List<NoArvore> arvore = new ArrayList<>();
        visitar(estrutura, raiz.codigo(), quantidade, 0, acumulados, arvore);

        List<LinhaMaterial> materiais = new ArrayList<>();
        List<LinhaMaterial> montagens = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        BigDecimal custoMateriais = BigDecimal.ZERO;
        BigDecimal custoProcessos = BigDecimal.ZERO;

        for (Acumulado a : acumulados.values()) {
            Estrutura.Item item = estrutura.item(a.codigo).orElseThrow();
            boolean montagem = estrutura.ehMontagem(a.codigo);
            BigDecimal unitario = item.custoUnitario() == null ? BigDecimal.ZERO : item.custoUnitario();
            if (item.custoUnitario() == null && !montagem) {
                avisos.add("Material " + a.codigo + " sem custo unitário; considerado R$ 0,00.");
            }
            BigDecimal qtd = arredondar(a.quantidade);
            BigDecimal total = a.quantidade.multiply(unitario).setScale(2, RoundingMode.HALF_UP);
            LinhaMaterial linha = new LinhaMaterial(item.codigo(), descricaoOuCodigo(item), item.unidade(),
                    montagem ? Tipo.MONTAGEM : Tipo.MATERIAL, a.nivelMaisProfundo, qtd, unitario, total);
            if (montagem) {
                montagens.add(linha);
                custoProcessos = custoProcessos.add(total);
            } else {
                materiais.add(linha);
                custoMateriais = custoMateriais.add(total);
            }
        }
        materiais.sort(Comparator.comparing(LinhaMaterial::custoTotal).reversed().thenComparing(LinhaMaterial::codigo));
        montagens.sort(Comparator.comparingInt(LinhaMaterial::nivel).thenComparing(LinhaMaterial::codigo));

        return new Resultado(raiz.codigo(), quantidade, materiais, montagens, arvore, avisos, custoMateriais, custoProcessos);
    }

    private static void visitar(Estrutura e, String codigo, BigDecimal qtd, int nivel,
                                Map<String, Acumulado> acc, List<NoArvore> arvore) {
        Estrutura.Item item = e.item(codigo).orElseThrow();
        boolean montagem = e.ehMontagem(codigo);
        arvore.add(new NoArvore(codigo, descricaoOuCodigo(item), nivel,
                arredondar(qtd), montagem ? Tipo.MONTAGEM : Tipo.MATERIAL));
        Acumulado a = acc.computeIfAbsent(codigo, Acumulado::new);
        a.quantidade = a.quantidade.add(qtd);
        a.nivelMaisProfundo = Math.max(a.nivelMaisProfundo, nivel);

        for (Estrutura.Componente c : e.componentesDe(codigo)) {
            BigDecimal fator = BigDecimal.ONE.add(c.perdaPercentual().movePointLeft(2));
            visitar(e, c.filho(), qtd.multiply(c.quantidade()).multiply(fator), nivel + 1, acc, arvore);
        }
    }

    /** Arredonda para {@link #ESCALA} casas e remove zeros à direita sem cair em notação científica. */
    static BigDecimal arredondar(BigDecimal valor) {
        BigDecimal v = valor.setScale(ESCALA, RoundingMode.HALF_UP).stripTrailingZeros();
        return v.scale() < 0 ? v.setScale(0) : v;
    }

    private static String descricaoOuCodigo(Estrutura.Item item) {
        return item.descricao() == null || item.descricao().isBlank() ? item.codigo() : item.descricao();
    }

    private static final class Acumulado {
        final String codigo;
        BigDecimal quantidade = BigDecimal.ZERO;
        int nivelMaisProfundo;

        Acumulado(String codigo) {
            this.codigo = codigo;
        }
    }
}
