package com.arthurentsev.lynowbom.servico;

import com.arthurentsev.lynowbom.core.ErroEstrutura;
import com.arthurentsev.lynowbom.core.Estrutura;
import com.arthurentsev.lynowbom.core.Explosao;
import com.arthurentsev.lynowbom.core.LinhaEstrutura;
import com.arthurentsev.lynowbom.dominio.ComponenteEntity;
import com.arthurentsev.lynowbom.dominio.ComponenteRepository;
import com.arthurentsev.lynowbom.dominio.ItemEntity;
import com.arthurentsev.lynowbom.dominio.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Catálogo de itens e estruturas: importação, consulta e explosão. */
@Service
public class CatalogoService {

    public record ResumoImportacao(
            String arquivo,
            int linhasLidas,
            int itensNovos,
            int itensAtualizados,
            int componentes,
            List<String> produtos) {
    }

    private final ItemRepository itens;
    private final ComponenteRepository componentes;
    private final LeitorPlanilha leitor;

    public CatalogoService(ItemRepository itens, ComponenteRepository componentes, LeitorPlanilha leitor) {
        this.itens = itens;
        this.componentes = componentes;
        this.leitor = leitor;
    }

    /**
     * Importa uma planilha. Montagens presentes no arquivo têm a estrutura substituída;
     * as demais permanecem como estavam. A estrutura combinada é validada (inclusive ciclos)
     * antes de qualquer gravação.
     */
    @Transactional
    public ResumoImportacao importar(String nomeArquivo, byte[] conteudo) {
        List<LinhaEstrutura> linhas = leitor.ler(nomeArquivo, conteudo);
        Estrutura doArquivo = Estrutura.deLinhas(linhas);
        Set<String> paisDoArquivo = doArquivo.componentes().stream()
                .map(Estrutura.Componente::pai).collect(Collectors.toSet());

        Map<String, ItemEntity> existentes = itens.findAll().stream()
                .collect(Collectors.toMap(ItemEntity::getCodigo, i -> i, (a, b) -> a, LinkedHashMap::new));

        // Valida a combinação "banco + arquivo" antes de gravar.
        List<Estrutura.Componente> combinados = new ArrayList<>(componentes.findAll().stream()
                .filter(c -> !paisDoArquivo.contains(c.getPai()))
                .map(c -> new Estrutura.Componente(c.getPai(), c.getFilho(), c.getQuantidade(), c.getPerdaPercentual()))
                .toList());
        combinados.addAll(doArquivo.componentes());
        Map<String, Estrutura.Item> itensCombinados = new LinkedHashMap<>();
        existentes.values().forEach(i -> itensCombinados.put(i.getCodigo(), paraCore(i)));
        doArquivo.itens().forEach(i -> itensCombinados.merge(i.codigo(), i, (antigo, novo) -> novo));
        new Estrutura(itensCombinados.values(), combinados);

        int novos = 0;
        int atualizados = 0;
        for (Estrutura.Item i : doArquivo.itens()) {
            ItemEntity entidade = existentes.get(i.codigo());
            if (entidade == null) {
                entidade = new ItemEntity(i.codigo());
                novos++;
            } else {
                atualizados++;
            }
            entidade.atualizar(i.descricao(), i.unidade(), i.custoUnitario());
            itens.save(entidade);
        }
        if (!paisDoArquivo.isEmpty()) {
            componentes.removerDosPais(paisDoArquivo);
        }
        componentes.saveAll(doArquivo.componentes().stream()
                .map(c -> new ComponenteEntity(c.pai(), c.filho(), c.quantidade(), c.perdaPercentual()))
                .toList());

        return new ResumoImportacao(nomeArquivo, linhas.size(), novos, atualizados,
                doArquivo.componentes().size(),
                doArquivo.produtos().stream().map(Estrutura.Item::codigo).toList());
    }

    @Transactional(readOnly = true)
    public Estrutura estrutura() {
        List<Estrutura.Item> lista = itens.findAll().stream().map(CatalogoService::paraCore).toList();
        List<Estrutura.Componente> ligacoes = componentes.findAll().stream()
                .map(c -> new Estrutura.Componente(c.getPai(), c.getFilho(), c.getQuantidade(), c.getPerdaPercentual()))
                .toList();
        return new Estrutura(lista, ligacoes);
    }

    @Transactional(readOnly = true)
    public Explosao.Resultado explodir(String produto, BigDecimal quantidade) {
        Estrutura e = estrutura();
        String codigo = produto.trim().toUpperCase();
        if (!e.ehMontagem(codigo)) {
            if (e.item(codigo).isEmpty()) {
                throw new ErroEstrutura("Produto " + codigo + " não encontrado.");
            }
            throw new ErroEstrutura(codigo + " é um material, não tem estrutura para explodir.");
        }
        return Explosao.explodir(e, codigo, quantidade);
    }

    @Transactional
    public void limpar() {
        componentes.deleteAllInBatch();
        itens.deleteAllInBatch();
    }

    private static Estrutura.Item paraCore(ItemEntity i) {
        return new Estrutura.Item(i.getCodigo(), i.getDescricao(), i.getUnidade(), i.getCustoUnitario());
    }
}
