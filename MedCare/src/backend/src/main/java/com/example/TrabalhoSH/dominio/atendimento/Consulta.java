package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

/** Compartilha o fluxo das consultas, sem impô-lo à emergência ou à cirurgia. */
public abstract class Consulta extends Atendimento {

    private SituacaoConsulta situacao = SituacaoConsulta.AGENDADA;

    protected Consulta(LocalDate data, LocalTime horario, String pacienteId,
                       String medicoResponsavelId) {
        super(data, horario, pacienteId, medicoResponsavelId);
    }

    public final SituacaoConsulta getSituacao() {
        return situacao;
    }

    /**
     * Proposta adotada nesta etapa, sujeita à revisão do grupo:
     * agendada permite confirmação, cancelamento ou não comparecimento;
     * confirmada permite início, cancelamento ou não comparecimento;
     * em atendimento permite apenas conclusão. As demais situações são finais.
     * Um novo agendamento após cancelamento exige outro registro.
     * A especialidade do profissional é usada somente ao iniciar o atendimento.
     */
    public final void alterarSituacao(SituacaoConsulta novaSituacao, String especialidadeProfissional) {
        if (novaSituacao == null) {
            throw new IllegalArgumentException("A nova situação da consulta é obrigatória.");
        }
        if (novaSituacao == situacao) {
            return;
        }
        boolean permitida = switch (situacao) {
            case AGENDADA -> novaSituacao == SituacaoConsulta.CONFIRMADA
                    || novaSituacao == SituacaoConsulta.CANCELADA
                    || novaSituacao == SituacaoConsulta.NAO_COMPARECEU;
            case CONFIRMADA -> novaSituacao == SituacaoConsulta.EM_ATENDIMENTO
                    || novaSituacao == SituacaoConsulta.CANCELADA
                    || novaSituacao == SituacaoConsulta.NAO_COMPARECEU;
            case EM_ATENDIMENTO -> novaSituacao == SituacaoConsulta.CONCLUIDA;
            case CONCLUIDA, CANCELADA, NAO_COMPARECEU -> false;
        };
        if (!permitida) {
            throw new IllegalStateException(
                    "Não é permitido alterar a consulta de " + situacao + " para " + novaSituacao + ".");
        }
        // Validação polimórfica antes da atribuição: falhas preservam a situação.
        if (novaSituacao == SituacaoConsulta.EM_ATENDIMENTO) {
            validarCondicoes(especialidadeProfissional);
        }
        situacao = novaSituacao;
    }
}
