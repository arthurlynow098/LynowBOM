package com.arthurentsev.lynowbom.core;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Formação de preço a partir do custo explodido.
 *
 * <p>Margem é aplicada sobre o custo; impostos são calculados "por dentro", como em nota fiscal:
 * {@code preço = custo × (1 + margem) ÷ (1 − impostos)}.</p>
 */
public final class CalculoOrcamento {

    public record Parametros(BigDecimal margemPercentual, BigDecimal impostosPercentual, BigDecimal frete) {
        public Parametros {
            margemPercentual = margemPercentual == null ? BigDecimal.ZERO : margemPercentual;
            impostosPercentual = impostosPercentual == null ? BigDecimal.ZERO : impostosPercentual;
            frete = frete == null ? BigDecimal.ZERO : frete;
            if (margemPercentual.signum() < 0) {
                throw new ErroEstrutura("A margem não pode ser negativa.");
            }
            if (impostosPercentual.signum() < 0 || impostosPercentual.compareTo(BigDecimal.valueOf(100)) >= 0) {
                throw new ErroEstrutura("Os impostos devem ficar entre 0 e 99,99%.");
            }
            if (frete.signum() < 0) {
                throw new ErroEstrutura("O frete não pode ser negativo.");
            }
        }
    }

    public record Valores(
            BigDecimal custoMateriais,
            BigDecimal custoProcessos,
            BigDecimal custoTotal,
            BigDecimal valorMargem,
            BigDecimal valorImpostos,
            BigDecimal frete,
            BigDecimal precoTotal,
            BigDecimal precoUnitario) {
    }

    private CalculoOrcamento() {
    }

    public static Valores calcular(Explosao.Resultado r, Parametros p) {
        BigDecimal custo = r.custoTotal();
        BigDecimal margem = custo.multiply(p.margemPercentual()).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
        BigDecimal base = custo.add(margem).add(p.frete());
        BigDecimal divisor = BigDecimal.ONE.subtract(p.impostosPercentual().movePointLeft(2));
        BigDecimal preco = base.divide(divisor, MathContext.DECIMAL64).setScale(2, RoundingMode.HALF_UP);
        BigDecimal impostos = preco.subtract(base);
        BigDecimal unitario = preco.divide(r.quantidadePedida(), 2, RoundingMode.HALF_UP);
        return new Valores(r.custoMateriais(), r.custoProcessos(), custo, margem, impostos, p.frete(), preco, unitario);
    }
}
