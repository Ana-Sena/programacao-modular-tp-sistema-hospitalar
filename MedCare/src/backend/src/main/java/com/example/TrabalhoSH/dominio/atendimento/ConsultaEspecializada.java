package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

/** Consulta direcionada a uma especialidade, com observações opcionais. */
public class ConsultaEspecializada extends Atendimento {

    private final String especialidade;
    private final String observacoes;

    public ConsultaEspecializada(LocalDate data, LocalTime horario, String pacienteId,
                            String medicoResponsavelId, String especialidade, String observacoes) {
        super(data, horario, pacienteId, medicoResponsavelId);
        this.especialidade = especialidade;
        this.observacoes = observacoes;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    @Override
    protected void validarDadosEspecificos() {
        exigirTexto(especialidade, "Especialidade");
    }
}
