package com.arthurentsev.lynowbom.core;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converte uma tabela de texto (vinda de CSV ou XLSX) em linhas de estrutura.
 *
 * <p>A primeira linha é o cabeçalho. Os nomes aceitam acento, maiúsculas, unidades entre
 * parênteses e sinônimos comuns em planilhas de engenharia ("Qtd", "Item pai", "Refugo").</p>
 */
public final class LeitorTabela {

    static final List<String> COLUNAS_OBRIGATORIAS = List.of("pai", "codigo", "descricao", "quantidade");

    private LeitorTabela() {
    }

    public static List<LinhaEstrutura> interpretar(List<List<String>> tabela) {
        if (tabela.isEmpty()) {
            throw new ErroEstrutura("A planilha está vazia.");
        }
        List<String> nomes = tabela.get(0).stream().map(LeitorTabela::normalizarCabecalho).toList();
        Map<String, Integer> indice = new HashMap<>();
        for (int i = 0; i < nomes.size(); i++) {
            indice.putIfAbsent(nomes.get(i), i);
        }
        List<String> faltando = COLUNAS_OBRIGATORIAS.stream().filter(c -> !indice.containsKey(c)).toList();
        if (!faltando.isEmpty()) {
            throw new ErroEstrutura("Colunas obrigatórias ausentes: " + String.join(", ", faltando)
                    + ". Baixe o modelo em /api/modelo.csv ou pelo painel.");
        }

        List<LinhaEstrutura> linhas = new ArrayList<>();
        List<String> erros = new ArrayList<>();
        for (int i = 1; i < tabela.size(); i++) {
            List<String> campos = tabela.get(i);
            int numero = i + 1;
            if (campos.stream().allMatch(c -> c == null || c.isBlank())) {
                continue;
            }
            try {
                linhas.add(new LinhaEstrutura(
                        numero,
                        campo(campos, indice, "pai"),
                        exigir(campo(campos, indice, "codigo"), "código"),
                        campo(campos, indice, "descricao"),
                        Numeros.ler(campo(campos, indice, "quantidade"), BigDecimal.ONE),
                        campo(campos, indice, "unidade"),
                        Numeros.ler(campo(campos, indice, "custo_unitario"), null),
                        Numeros.ler(campo(campos, indice, "perda"), BigDecimal.ZERO)));
            } catch (IllegalArgumentException e) {
                erros.add("Linha " + numero + ": " + e.getMessage());
            }
        }
        if (!erros.isEmpty()) {
            throw new ErroEstrutura("A planilha tem valores inválidos.", erros);
        }
        if (linhas.isEmpty()) {
            throw new ErroEstrutura("A planilha não tem linhas de estrutura.");
        }
        return linhas;
    }

    private static String exigir(String valor, String nome) {
        if (valor.isBlank()) {
            throw new IllegalArgumentException("o campo " + nome + " é obrigatório");
        }
        return valor;
    }

    private static String campo(List<String> campos, Map<String, Integer> indice, String nome) {
        Integer i = indice.get(nome);
        if (i == null || i >= campos.size() || campos.get(i) == null) {
            return "";
        }
        return campos.get(i).trim();
    }

    /** Converte "Custo Unitário (R$)" em "custo_unitario" e "Perda %" em "perda". */
    static String normalizarCabecalho(String bruto) {
        String s = Normalizer.normalize(bruto == null ? "" : bruto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\(.*?\\)", "")
                .replace("%", "")
                .trim()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        return switch (s) {
            case "item_pai", "montagem", "conjunto" -> "pai";
            case "item", "cod", "codigo_item" -> "codigo";
            case "qtd", "qtde", "quant" -> "quantidade";
            case "un", "und" -> "unidade";
            case "custo", "preco_unitario", "custo_unit" -> "custo_unitario";
            case "perda_percentual", "refugo" -> "perda";
            default -> s;
        };
    }
}
