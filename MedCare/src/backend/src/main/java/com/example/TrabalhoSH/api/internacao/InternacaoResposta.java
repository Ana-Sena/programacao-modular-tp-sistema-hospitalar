package com.example.TrabalhoSH.api.internacao;

import com.example.TrabalhoSH.dominio.internacao.Internacao;
import com.example.TrabalhoSH.dominio.internacao.SituacaoInternacao;
import java.time.LocalDate;

public record InternacaoResposta(String id, String pacienteId, String medicoResponsavelId, String quartoId,
                                 LocalDate dataEntrada, LocalDate dataPrevistaAlta, LocalDate dataEfetivaAlta,
                                 String observacoes, SituacaoInternacao situacao) {

    public static InternacaoResposta de(Internacao internacao) {
        return new InternacaoResposta(internacao.getId(), internacao.getPaciente().getId(),
                internacao.getMedicoResponsavelId(), internacao.getQuarto().getId(), internacao.getDataEntrada(),
                internacao.getDataPrevistaAlta(), internacao.getDataEfetivaAlta(), internacao.getObservacoes(),
                internacao.getSituacao());
    }
}
