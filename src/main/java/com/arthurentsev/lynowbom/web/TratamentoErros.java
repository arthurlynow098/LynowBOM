package com.arthurentsev.lynowbom.web;

import com.arthurentsev.lynowbom.core.ErroEstrutura;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

/** Converte erros de domínio em respostas RFC 7807 com mensagens em português. */
@RestControllerAdvice
public class TratamentoErros {

    @ExceptionHandler(ErroEstrutura.class)
    public ProblemDetail estrutura(ErroEstrutura e) {
        HttpStatus status = e.getMessage() != null && e.getMessage().contains("não encontrado")
                ? HttpStatus.NOT_FOUND : HttpStatus.UNPROCESSABLE_ENTITY;
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        p.setTitle("Estrutura inválida");
        if (!e.getDetalhes().isEmpty()) {
            p.setProperty("detalhes", e.getDetalhes());
        }
        return p;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e) {
        List<String> detalhes = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        ProblemDetail p = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Pedido de orçamento inválido.");
        p.setTitle("Dados inválidos");
        p.setProperty("detalhes", detalhes);
        return p;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail tamanho(MaxUploadSizeExceededException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.PAYLOAD_TOO_LARGE, "A planilha passa do limite de 10 MB.");
    }
}
