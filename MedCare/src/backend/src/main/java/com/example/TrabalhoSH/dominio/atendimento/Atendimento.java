package com.example.TrabalhoSH.dominio.atendimento;

import java.math.BigDecimal;
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
     * Verifica o preenchimento da estrutura e a positividade de IDs numéricos.
     * Não verifica existência dos IDs, disponibilidade ou compatibilidade médica.
     * @throws IllegalArgumentException se um dado obrigatório estiver ausente
     */
    public final void validarEstrutura() {
        if (data == null || horario == null) {
            throw new IllegalArgumentException("Data e horário são obrigatórios.");
        }
        validarIdentificador(pacienteId, "Identificador do paciente");
        validarIdentificador(medicoResponsavelId, "Identificador do médico responsável");
        validarDadosEspecificos();
    }

    /**
     * Valida as condições para realizar a modalidade, sem executar o atendimento.
     * A especialidade deve vir do profissional identificado por medicoResponsavelId;
     * ela é obrigatória apenas para consulta especializada e não é armazenada aqui.
     * O chamador será responsável por obter esse dado do módulo de profissionais.
     * @throws IllegalArgumentException quando uma condição não for atendida
     */
    public abstract void validarCondicoes(String especialidadeProfissional);

    private static void validarIdentificador(String valor, String campo) {
        exigirTexto(valor, campo);
        BigDecimal numero;
        try {
            // Mantém IDs textuais (como UUIDs), mas exige positividade dos numéricos.
            numero = new BigDecimal(valor.strip());
        } catch (NumberFormatException identificadorTextual) {
            return;
        }
        if (numero.signum() <= 0) {
            throw new IllegalArgumentException(campo + " deve ser positivo quando numérico.");
        }
    }

    /** Cada modalidade verifica seus próprios dados, sem repetir os dados comuns. */
    protected abstract void validarDadosEspecificos();

    protected static void exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
    }
}
