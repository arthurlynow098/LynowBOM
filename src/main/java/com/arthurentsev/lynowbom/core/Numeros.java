package com.arthurentsev.lynowbom.core;

import java.math.BigDecimal;

/** Leitura de números no formato brasileiro ou internacional. */
public final class Numeros {

    private Numeros() {
    }

    /**
     * Interpreta "1.234,56", "1234.56", "1234,5" ou "12%". Retorna {@code padrao} para texto vazio.
     *
     * @throws IllegalArgumentException quando o texto não é um número
     */
    public static BigDecimal ler(String texto, BigDecimal padrao) {
        if (texto == null || texto.isBlank()) {
            return padrao;
        }
        String s = texto.trim().replace("R$", "").replace("%", "").replace(" ", "").replace(" ", "");
        int virgula = s.lastIndexOf(',');
        int ponto = s.lastIndexOf('.');
        if (virgula >= 0 && ponto >= 0) {
            if (virgula > ponto) {
                s = s.replace(".", "").replace(',', '.');
            } else {
                s = s.replace(",", "");
            }
        } else if (virgula >= 0) {
            s = s.replace(',', '.');
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + texto.trim() + "\" não é um número válido");
        }
    }
}
