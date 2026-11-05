package com.example.TrabalhoSH.api.consulta;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public record AlteracaoSituacao(String situacao) {

    @JsonAnySetter
    public void rejeitarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido na alteração de situação: " + campo + ".");
    }
}
