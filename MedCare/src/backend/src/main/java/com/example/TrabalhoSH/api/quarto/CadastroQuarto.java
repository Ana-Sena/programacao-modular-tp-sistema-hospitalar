package com.example.TrabalhoSH.api.quarto;

import com.example.TrabalhoSH.dominio.internacao.TipoQuarto;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public record CadastroQuarto(String numero, Integer andar, TipoQuarto tipo, Integer capacidadeMaxima) {

    @JsonAnySetter
    public void rejeitarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido no cadastro: " + campo + ".");
    }
}
