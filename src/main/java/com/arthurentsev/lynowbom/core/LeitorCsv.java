package com.arthurentsev.lynowbom.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Lê planilhas de estrutura exportadas em CSV.
 *
 * <p>Aceita separador {@code ;} ou {@code ,} e campos entre aspas. É o formato que o Excel
 * gera em português ao salvar como "CSV (separado por ponto e vírgula)". A interpretação
 * das colunas fica em {@link LeitorTabela}, compartilhada com a leitura de XLSX.</p>
 */
public final class LeitorCsv {

    private LeitorCsv() {
    }

    public static List<LinhaEstrutura> ler(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String cabecalho = in.readLine();
        if (cabecalho == null) {
            throw new ErroEstrutura("A planilha está vazia.");
        }
        if (cabecalho.startsWith("﻿")) {
            cabecalho = cabecalho.substring(1);
        }
        char separador = cabecalho.chars().filter(c -> c == ';').count()
                >= cabecalho.chars().filter(c -> c == ',').count() ? ';' : ',';

        List<List<String>> linhas = new ArrayList<>();
        linhas.add(dividir(cabecalho, separador));
        String bruta;
        while ((bruta = in.readLine()) != null) {
            linhas.add(dividir(bruta, separador));
        }
        return LeitorTabela.interpretar(linhas);
    }

    static List<String> dividir(String linha, char separador) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean aspas = false;
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                if (aspas && i + 1 < linha.length() && linha.charAt(i + 1) == '"') {
                    atual.append('"');
                    i++;
                } else {
                    aspas = !aspas;
                }
            } else if (c == separador && !aspas) {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos;
    }
}
