package com.example.TrabalhoSH.api.paciente;

import com.example.TrabalhoSH.dominio.paciente.Endereco;
import com.fasterxml.jackson.annotation.JsonAnySetter;

/** Atualiza os dados de contato; CPF, nascimento e ID preservam a identificação. */
public record AtualizacaoPaciente(String nome, String telefone, String email, Endereco endereco) {

    @JsonAnySetter
    public void rejeitarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido na atualização: " + campo + ".");
    }
}
