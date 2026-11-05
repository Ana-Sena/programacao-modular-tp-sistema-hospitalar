package com.example.TrabalhoSH.api.consulta;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.time.LocalDate;
import java.time.LocalTime;

/** Não recebe ID, situação inicial nem dados do profissional como comprovação. */
public record CadastroConsulta(String modalidade, LocalDate data, LocalTime horario,
        String pacienteId, String medicoResponsavelId, String motivo, String observacoes,
        String especialidade, String prioridade) {

    @JsonAnySetter
    public void rejeitarCampo(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido no cadastro: " + campo + ".");
    }
}
