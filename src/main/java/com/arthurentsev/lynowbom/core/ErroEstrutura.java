package com.arthurentsev.lynowbom.core;

import java.util.List;

/** Erro de validação de uma estrutura de produto, com as mensagens por linha. */
public class ErroEstrutura extends RuntimeException {

    private final List<String> detalhes;

    public ErroEstrutura(String mensagem, List<String> detalhes) {
        super(mensagem);
        this.detalhes = List.copyOf(detalhes);
    }

    public ErroEstrutura(String mensagem) {
        this(mensagem, List.of());
    }

    public List<String> getDetalhes() {
        return detalhes;
    }
}
