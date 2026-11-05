package com.example.TrabalhoSH.api.consulta;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Tratamento limitado à API de consultas. */
@RestControllerAdvice(assignableTypes = ConsultaController.class)
public class ConsultaErros {

    public record Erro(String mensagem) { }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Erro> entradaInvalida(IllegalArgumentException erro) {
        return ResponseEntity.badRequest().body(new Erro(erro.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Erro> conflito(IllegalStateException erro) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new Erro(erro.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Erro> operacaoIndisponivel(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(new Erro(erro.getReason()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> jsonInvalido(HttpMessageNotReadableException erro) {
        Throwable causa = erro;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        String mensagem = causa.getMessage();
        if (mensagem == null || !mensagem.startsWith("Campo não permitido")) {
            mensagem = "JSON inválido. Confira os campos e tipos; use data AAAA-MM-DD e horário HH:mm:ss.";
        }
        return ResponseEntity.badRequest().body(new Erro(mensagem));
    }
}
