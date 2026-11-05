package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dados comuns às modalidades de atendimento, inclusive à futura cirurgia.
 * Os identificadores evitam duplicar Paciente e ProfissionalSaude, que serão
 * implementados por outros módulos. Futuramente poderão virar associações.
 */
public abstract class Atendimento {

    private final LocalDate data;
    private final LocalTime horario;
    private final String pacienteId;
    private final String medicoResponsavelId;

    protected Atendimento(LocalDate data, LocalTime horario, String pacienteId,
                          String medicoResponsavelId) {
        this.data = data;
        this.horario = horario;
        this.pacienteId = pacienteId;
        this.medicoResponsavelId = medicoResponsavelId;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public String getMedicoResponsavelId() {
        return medicoResponsavelId;
    }

    /**
     * Verifica somente o preenchimento da estrutura, quando chamado explicitamente.
     * Não verifica existência dos IDs, disponibilidade ou compatibilidade médica.
     * @throws IllegalArgumentException se um dado obrigatório estiver ausente
     */
    public final void validarEstrutura() {
        if (data == null || horario == null) {
            throw new IllegalArgumentException("Data e horário são obrigatórios.");
        }
        exigirTexto(pacienteId, "Identificador do paciente");
        exigirTexto(medicoResponsavelId, "Identificador do médico responsável");
        validarDadosEspecificos();
    }

    /** Cada modalidade verifica seus próprios dados, sem repetir os dados comuns. */
    protected abstract void validarDadosEspecificos();

    protected static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
    }
}
