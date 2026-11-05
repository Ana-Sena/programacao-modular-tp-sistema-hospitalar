package com.example.TrabalhoSH.api.internacao;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.time.LocalDate;

public record CadastroInternacao(String pacienteId, String medicoResponsavelId, String quartoId,
                                 LocalDate dataEntrada, LocalDate dataPrevistaAlta, String observacoes) {

    @JsonAnySetter
    public void rejeitarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido no cadastro: " + campo + ".");
    }
}
