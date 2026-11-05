package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

/** Atendimento de emergência com uma prioridade definida. */
public class AtendimentoEmergencia extends Atendimento {

    private final PrioridadeEmergencia prioridade;
    private SituacaoEmergencia situacao = SituacaoEmergencia.AGUARDANDO_AVALIACAO;

    public AtendimentoEmergencia(LocalDate data, LocalTime horario, String pacienteId,
                            String medicoResponsavelId, PrioridadeEmergencia prioridade) {
        super(data, horario, pacienteId, medicoResponsavelId);
        this.prioridade = prioridade;
    }

    public PrioridadeEmergencia getPrioridade() {
        return prioridade;
    }

    public SituacaoEmergencia getSituacao() {
        return situacao;
    }

    /** Início imediato a partir da espera, sem agendamento ou confirmação prévios. */
    public final void iniciarAtendimento() {
        if (situacao == SituacaoEmergencia.EM_ATENDIMENTO) {
            return;
        }
        if (situacao != SituacaoEmergencia.AGUARDANDO_AVALIACAO) {
            throw new IllegalStateException("Uma emergência concluída não pode ser reiniciada.");
        }
        // Não altera o estado se os dados obrigatórios ou a prioridade forem inválidos.
        validarCondicoes(null);
        situacao = SituacaoEmergencia.EM_ATENDIMENTO;
    }

    public final void concluirAtendimento() {
        if (situacao == SituacaoEmergencia.CONCLUIDA) {
            return;
        }
        if (situacao != SituacaoEmergencia.EM_ATENDIMENTO) {
            throw new IllegalStateException("A emergência precisa estar em atendimento para ser concluída.");
        }
        situacao = SituacaoEmergencia.CONCLUIDA;
    }

    @Override
    public void validarCondicoes(String especialidadeProfissional) {
        validarEstrutura();
    }

    @Override
    protected void validarDadosEspecificos() {
        if (prioridade == null) {
            throw new IllegalArgumentException("Prioridade é obrigatória.");
        }
    }
}
