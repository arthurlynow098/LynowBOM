package com.arthurentsev.lynowbom.core;

import java.math.BigDecimal;

/**
 * Uma linha da planilha de estrutura de produto.
 *
 * <p>Quando {@code pai} está vazio, a linha descreve um item de topo (produto vendável).
 * Caso contrário, informa que {@code codigo} entra em {@code pai} na {@code quantidade} indicada.</p>
 */
public record LinhaEstrutura(
        int numeroLinha,
        String pai,
        String codigo,
        String descricao,
        BigDecimal quantidade,
        String unidade,
        BigDecimal custoUnitario,
        BigDecimal perdaPercentual) {

    public boolean ehTopo() {
        return pai == null || pai.isBlank();
    }
}
