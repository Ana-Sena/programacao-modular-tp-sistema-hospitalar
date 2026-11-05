package com.example.TrabalhoSH.api.paciente;

import com.example.TrabalhoSH.dominio.paciente.Endereco;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.time.LocalDate;

public record CadastroPaciente(String nome, String cpf, LocalDate dataNascimento,
                               String telefone, String email, Endereco endereco) {

    @JsonAnySetter
    public void rejeitarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido no cadastro: " + campo + ".");
    }
}
