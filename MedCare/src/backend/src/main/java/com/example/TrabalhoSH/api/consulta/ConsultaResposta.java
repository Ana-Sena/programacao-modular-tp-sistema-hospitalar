package com.example.TrabalhoSH.api.consulta;

import com.example.TrabalhoSH.dominio.atendimento.*;
import java.time.LocalDate;
import java.time.LocalTime;

/** Retrato do registro; não expõe o objeto mutável guardado no serviço. */
public record ConsultaResposta(String id, ModalidadeConsulta modalidade, LocalDate data,
        LocalTime horario, String pacienteId, String medicoResponsavelId, String motivo,
        String observacoes, String especialidade, PrioridadeEmergencia prioridade, String situacao) {

    public static ConsultaResposta de(String id, Atendimento atendimento) {
        if (atendimento instanceof ConsultaRotina rotina) {
            return new ConsultaResposta(id, ModalidadeConsulta.ROTINA, rotina.getData(), rotina.getHorario(),
                    rotina.getPacienteId(), rotina.getMedicoResponsavelId(), rotina.getMotivo(),
                    rotina.getObservacoes(), null, null, rotina.getSituacao().name());
        }
        if (atendimento instanceof ConsultaEspecializada especializada) {
            return new ConsultaResposta(id, ModalidadeConsulta.ESPECIALIZADA, especializada.getData(),
                    especializada.getHorario(), especializada.getPacienteId(), especializada.getMedicoResponsavelId(),
                    null, especializada.getObservacoes(), especializada.getEspecialidade(), null,
                    especializada.getSituacao().name());
        }
        AtendimentoEmergencia emergencia = (AtendimentoEmergencia) atendimento;
        return new ConsultaResposta(id, ModalidadeConsulta.EMERGENCIA, emergencia.getData(), emergencia.getHorario(),
                emergencia.getPacienteId(), emergencia.getMedicoResponsavelId(), null, null, null,
                emergencia.getPrioridade(), emergencia.getSituacao().name());
    }
}
