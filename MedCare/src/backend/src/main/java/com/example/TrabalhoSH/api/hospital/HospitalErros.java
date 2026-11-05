package com.example.TrabalhoSH.api.hospital;

import com.example.TrabalhoSH.api.internacao.InternacaoController;
import com.example.TrabalhoSH.api.paciente.PacienteController;
import com.example.TrabalhoSH.api.quarto.QuartoController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Tratamento limitado aos módulos de pacientes, internações e quartos. */
@RestControllerAdvice(assignableTypes = {PacienteController.class, InternacaoController.class, QuartoController.class})
public class HospitalErros {

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
    public ResponseEntity<Erro> naoEncontrado(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(new Erro(erro.getReason()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> jsonInvalido(HttpMessageNotReadableException erro) {
        return ResponseEntity.badRequest().body(new Erro(
                "JSON inválido. Confira os campos e tipos, datas AAAA-MM-DD e valores dos enums."));
    }
}
