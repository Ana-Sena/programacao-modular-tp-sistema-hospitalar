package com.example.TrabalhoSH.api.internacao;

import com.example.TrabalhoSH.dominio.internacao.SituacaoInternacao;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.time.LocalDate;

public record AlteracaoSituacao(SituacaoInternacao situacao, LocalDate dataEfetivaAlta) {

    @JsonAnySetter
    public void rejeitarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido na alteração: " + campo + ".");
    }
}
