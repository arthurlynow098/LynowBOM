package com.arthurentsev.lynowbom.core;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Árvore (grafo acíclico) de itens e componentes, independente de banco de dados.
 *
 * <p>Cada item tem custo próprio: para materiais é o preço de compra; para montagens
 * representa o custo do processo (mão de obra, solda, pintura) por unidade montada.</p>
 */
public final class Estrutura {

    public record Item(String codigo, String descricao, String unidade, BigDecimal custoUnitario) {
    }

    public record Componente(String pai, String filho, BigDecimal quantidade, BigDecimal perdaPercentual) {
    }

    private final Map<String, Item> itens = new LinkedHashMap<>();
    private final Map<String, List<Componente>> filhos = new LinkedHashMap<>();
    private final Set<String> temPai = new HashSet<>();

    public Estrutura(Collection<Item> itens, Collection<Componente> componentes) {
        itens.forEach(i -> this.itens.put(i.codigo(), i));
        for (Componente c : componentes) {
            filhos.computeIfAbsent(c.pai(), k -> new ArrayList<>()).add(c);
            temPai.add(c.filho());
        }
        validar();
    }

    /** Monta a estrutura a partir das linhas da planilha, consolidando itens repetidos. */
    public static Estrutura deLinhas(List<LinhaEstrutura> linhas) {
        Map<String, Item> itens = new LinkedHashMap<>();
        Map<String, Componente> componentes = new LinkedHashMap<>();
        List<String> erros = new ArrayList<>();

        for (LinhaEstrutura l : linhas) {
            String codigo = l.codigo().trim().toUpperCase();
            itens.merge(codigo, new Item(codigo, l.descricao(), l.unidade(), l.custoUnitario()), Estrutura::mesclar);
            if (l.ehTopo()) {
                continue;
            }
            String pai = l.pai().trim().toUpperCase();
            itens.putIfAbsent(pai, new Item(pai, "", "", null));
            if (l.quantidade().signum() <= 0) {
                erros.add("Linha " + l.numeroLinha() + ": quantidade de " + codigo + " em " + pai + " deve ser maior que zero");
            }
            if (l.perdaPercentual().signum() < 0 || l.perdaPercentual().compareTo(BigDecimal.valueOf(100)) >= 0) {
                erros.add("Linha " + l.numeroLinha() + ": perda deve ficar entre 0 e 99,99%");
            }
            String chave = pai + "→" + codigo;
            if (componentes.containsKey(chave)) {
                erros.add("Linha " + l.numeroLinha() + ": " + codigo + " já aparece em " + pai + "; some as quantidades em uma linha só");
            }
            componentes.put(chave, new Componente(pai, codigo, l.quantidade(), l.perdaPercentual()));
        }
        if (!erros.isEmpty()) {
            throw new ErroEstrutura("A estrutura tem inconsistências.", erros);
        }
        return new Estrutura(itens.values(), componentes.values());
    }

    private static Item mesclar(Item atual, Item novo) {
        return new Item(
                atual.codigo(),
                vazio(atual.descricao()) ? novo.descricao() : atual.descricao(),
                vazio(atual.unidade()) ? novo.unidade() : atual.unidade(),
                atual.custoUnitario() == null ? novo.custoUnitario() : atual.custoUnitario());
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private void validar() {
        // Detecção de ciclos com DFS iterativa (branco/cinza/preto).
        Map<String, Integer> estado = new LinkedHashMap<>();
        for (String inicio : itens.keySet()) {
            if (estado.getOrDefault(inicio, 0) != 0) {
                continue;
            }
            Deque<Object[]> pilha = new ArrayDeque<>();
            Deque<String> caminho = new ArrayDeque<>();
            pilha.push(new Object[]{inicio, 0});
            estado.put(inicio, 1);
            caminho.addLast(inicio);
            while (!pilha.isEmpty()) {
                Object[] topo = pilha.peek();
                String atual = (String) topo[0];
                int proximo = (int) topo[1];
                List<Componente> lista = componentesDe(atual);
                if (proximo < lista.size()) {
                    topo[1] = proximo + 1;
                    String filho = lista.get(proximo).filho();
                    int e = estado.getOrDefault(filho, 0);
                    if (e == 1) {
                        List<String> ciclo = new ArrayList<>(caminho);
                        ciclo = ciclo.subList(ciclo.indexOf(filho), ciclo.size());
                        ciclo.add(filho);
                        throw new ErroEstrutura("A estrutura tem um ciclo: " + String.join(" → ", ciclo));
                    }
                    if (e == 0) {
                        estado.put(filho, 1);
                        caminho.addLast(filho);
                        pilha.push(new Object[]{filho, 0});
                    }
                } else {
                    estado.put(atual, 2);
                    caminho.removeLast();
                    pilha.pop();
                }
            }
        }
    }

    public Collection<Item> itens() {
        return itens.values();
    }

    public Optional<Item> item(String codigo) {
        return Optional.ofNullable(itens.get(codigo.trim().toUpperCase()));
    }

    public List<Componente> componentesDe(String codigo) {
        return filhos.getOrDefault(codigo, List.of());
    }

    public List<Componente> componentes() {
        return filhos.values().stream().flatMap(List::stream).toList();
    }

    public boolean ehMontagem(String codigo) {
        return !componentesDe(codigo).isEmpty();
    }

    /** Itens que não entram em nenhum outro: os produtos que podem ser orçados. */
    public List<Item> produtos() {
        return itens.values().stream().filter(i -> !temPai.contains(i.codigo()) && ehMontagem(i.codigo())).toList();
    }
}
