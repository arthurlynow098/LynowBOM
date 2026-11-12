package com.arthurentsev.lynowbom.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExplosaoTest {

    private static Estrutura exemplo() throws IOException {
        var in = ExplosaoTest.class.getResourceAsStream("/exemplos/estrutura-exemplo.csv");
        return Estrutura.deLinhas(LeitorCsv.ler(new InputStreamReader(in, StandardCharsets.UTF_8)));
    }

    private static Explosao.LinhaMaterial material(Explosao.Resultado r, String codigo) {
        return r.materiais().stream().filter(m -> m.codigo().equals(codigo)).findFirst().orElseThrow();
    }

    @Test
    void encontraOsProdutosDeTopo() throws IOException {
        assertThat(exemplo().produtos()).extracting(Estrutura.Item::codigo).containsExactly("QDC-12", "GC-1000");
    }

    @Test
    void multiplicaPelosNiveisEAplicaPerda() throws IOException {
        Explosao.Resultado r = Explosao.explodir(exemplo(), "QDC-12", new BigDecimal("10"));
        // 10 quadros × 1 caixa × 1,35 m² × 1,12 de perda
        assertThat(material(r, "CHAPA-14").quantidade()).isEqualByComparingTo("15.12");
        // 10 × 6 m × 1,10
        assertThat(material(r, "CABO-10").quantidade()).isEqualByComparingTo("66");
    }

    @Test
    void somaMaterialRepetidoEmMontagensDiferentes() throws IOException {
        Explosao.Resultado r = Explosao.explodir(exemplo(), "QDC-12", BigDecimal.ONE);
        // 8 na caixa + 12 no barramento
        assertThat(material(r, "PARAF-M6").quantidade()).isEqualByComparingTo("20");
        assertThat(r.materiais()).extracting(Explosao.LinhaMaterial::codigo).doesNotHaveDuplicates();
    }

    @Test
    void separaCustoDeMateriaisECustoDeProcessos() throws IOException {
        Explosao.Resultado r = Explosao.explodir(exemplo(), "QDC-12", new BigDecimal("10"));
        assertThat(r.custoMateriais()).isEqualByComparingTo("9795.70");
        assertThat(r.custoProcessos()).isEqualByComparingTo("2250.00");
        assertThat(r.materiais().get(0).codigo()).isEqualTo("DJ-MONO-20");
    }

    @Test
    void calculaPrecoComImpostosPorDentro() throws IOException {
        Explosao.Resultado r = Explosao.explodir(exemplo(), "QDC-12", new BigDecimal("10"));
        CalculoOrcamento.Valores v = CalculoOrcamento.calcular(r, new CalculoOrcamento.Parametros(
                new BigDecimal("25"), new BigDecimal("18"), new BigDecimal("150")));
        assertThat(v.valorMargem()).isEqualByComparingTo("3011.43");
        assertThat(v.precoTotal()).isEqualByComparingTo("18545.28");
        assertThat(v.valorImpostos()).isEqualByComparingTo("3338.15");
        assertThat(v.precoUnitario()).isEqualByComparingTo("1854.53");
    }

    @Test
    void recusaCiclo() {
        String csv = "pai;codigo;descricao;quantidade\n;A;a;1\nA;B;b;1\nB;C;c;2\nC;A;a;1\n";
        assertThatThrownBy(() -> Estrutura.deLinhas(LeitorCsv.ler(new StringReader(csv))))
                .isInstanceOf(ErroEstrutura.class)
                .hasMessageContaining("ciclo")
                .hasMessageContaining("A → B → C → A");
    }

    @Test
    void apontaLinhaComNumeroInvalido() {
        String csv = "pai,codigo,descricao,quantidade\n,A,a,1\nA,B,b,dois\n";
        assertThatThrownBy(() -> LeitorCsv.ler(new StringReader(csv)))
                .isInstanceOfSatisfying(ErroEstrutura.class,
                        e -> assertThat(e.getDetalhes()).containsExactly("Linha 3: \"dois\" não é um número válido"));
    }

    @Test
    void recusaLigacaoDuplicadaEQuantidadeZero() {
        String csv = "pai;codigo;descricao;quantidade\n;A;a;1\nA;B;b;1\nA;B;b;2\nA;C;c;0\n";
        assertThatThrownBy(() -> Estrutura.deLinhas(LeitorCsv.ler(new StringReader(csv))))
                .isInstanceOfSatisfying(ErroEstrutura.class, e -> assertThat(e.getDetalhes()).hasSize(2));
    }

    @Test
    void aceitaCabecalhosComAcentoESinonimos() throws IOException {
        String csv = "Item pai;Código;Descrição;Qtd;Un;Custo unitário (R$);Refugo %\n;P;Produto;1;un;;\nP;M;Mat;2,5;kg;1.234,50;10\n";
        List<LinhaEstrutura> linhas = LeitorCsv.ler(new StringReader(csv));
        assertThat(linhas.get(1).quantidade()).isEqualByComparingTo("2.5");
        assertThat(linhas.get(1).custoUnitario()).isEqualByComparingTo("1234.50");
        assertThat(linhas.get(1).perdaPercentual()).isEqualByComparingTo("10");
    }

    @Test
    void avisaMaterialSemCusto() throws IOException {
        String csv = "pai;codigo;descricao;quantidade;custo_unitario\n;P;Produto;1;\nP;M;Material;3;\n";
        Explosao.Resultado r = Explosao.explodir(Estrutura.deLinhas(LeitorCsv.ler(new StringReader(csv))), "P", BigDecimal.ONE);
        assertThat(r.avisos()).containsExactly("Material M sem custo unitário; considerado R$ 0,00.");
    }
}
