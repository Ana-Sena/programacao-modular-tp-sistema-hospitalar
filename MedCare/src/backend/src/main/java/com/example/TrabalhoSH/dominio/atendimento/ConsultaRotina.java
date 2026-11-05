package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

/** Consulta de rotina com motivo e observações opcionais. */
public class ConsultaRotina extends Atendimento {

    private final String motivo;
    private final String observacoes;

    public ConsultaRotina(LocalDate data, LocalTime horario, String pacienteId,
                            String medicoResponsavelId, String motivo, String observacoes) {
        super(data, horario, pacienteId, medicoResponsavelId);
        this.motivo = motivo;
        this.observacoes = observacoes;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getObservacoes() {
        return observacoes;
    }

    @Override
    public void validarCondicoes(String especialidadeProfissional) {
        validarEstrutura();
    }

    @Override
    protected void validarDadosEspecificos() {
        exigirTexto(motivo, "Motivo");
    }
}
