package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

/** Atendimento de emergência com uma prioridade definida. */
public class AtendimentoEmergencia extends Atendimento {

    private final PrioridadeEmergencia prioridade;

    public AtendimentoEmergencia(LocalDate data, LocalTime horario, String pacienteId,
                            String medicoResponsavelId, PrioridadeEmergencia prioridade) {
        super(data, horario, pacienteId, medicoResponsavelId);
        this.prioridade = prioridade;
    }

    public PrioridadeEmergencia getPrioridade() {
        return prioridade;
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
