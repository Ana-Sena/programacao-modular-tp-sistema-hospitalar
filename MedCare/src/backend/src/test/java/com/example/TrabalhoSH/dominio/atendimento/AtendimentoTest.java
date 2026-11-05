package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verificações de domínio sem carregar o contexto Spring. */
class AtendimentoTest {

    private final LocalDate data = LocalDate.of(2020, 1, 1);
    private final LocalTime horario = LocalTime.of(10, 30);

    @Test
    void aceitaModalidadesValidasComObservacoesOpcionaisEDataPassada() {
        Atendimento rotina = new ConsultaRotina(data, horario, "p1", "m1", "Retorno", null);
        Atendimento especializada = new ConsultaEspecializada(
                data, horario, "1", "2", " Cardiologia ", "");
        assertDoesNotThrow(() -> rotina.validarCondicoes(null));
        assertDoesNotThrow(() -> especializada.validarCondicoes("cardiologia"));
        for (PrioridadeEmergencia prioridade : PrioridadeEmergencia.values()) {
            Atendimento emergencia = new AtendimentoEmergencia(data, horario, "p1", "m1", prioridade);
            assertDoesNotThrow(() -> emergencia.validarCondicoes(null));
        }
    }

    @Test
    void rejeitaDadosComunsAusentes() {
        rejeitar(new ConsultaRotina(null, horario, "p1", "m1", "Retorno", null), null, "Data");
        rejeitar(new ConsultaRotina(data, null, "p1", "m1", "Retorno", null), null, "horário");
        for (String ausente : new String[]{null, "", "   "}) {
            rejeitar(new ConsultaRotina(data, horario, ausente, "m1", "Retorno", null), null, "paciente");
            rejeitar(new ConsultaRotina(data, horario, "p1", ausente, "Retorno", null), null, "médico");
        }
        rejeitar(new ConsultaEspecializada(data, null, "p1", "m1", "Cardiologia", null),
                "Cardiologia", "horário");
        rejeitar(new AtendimentoEmergencia(null, horario, "p1", "m1", PrioridadeEmergencia.ALTA),
                null, "Data");
    }

    @Test
    void rejeitaIdentificadoresNumericosNaoPositivos() {
        for (String id : new String[]{"0", "-1", " 0 ", "-999999999999999999999999"}) {
            rejeitar(new ConsultaRotina(data, horario, id, "m1", "Retorno", null), null, "positivo");
            rejeitar(new ConsultaRotina(data, horario, "p1", id, "Retorno", null), null, "positivo");
        }
    }

    @Test
    void rejeitaMotivoEspecialidadeEPrioridadeAusentes() {
        for (String ausente : new String[]{null, "", "   "}) {
            rejeitar(new ConsultaRotina(data, horario, "p1", "m1", ausente, null), null, "Motivo");
            rejeitar(new ConsultaEspecializada(data, horario, "p1", "m1", ausente, null),
                    "Cardiologia", "Especialidade");
            rejeitar(new ConsultaEspecializada(data, horario, "p1", "m1", "Cardiologia", null),
                    ausente, "Especialidade do médico");
        }
        rejeitar(new AtendimentoEmergencia(data, horario, "p1", "m1", null), null, "Prioridade");
    }

    @Test
    void rejeitaEspecialidadeIncompativel() {
        Atendimento consulta = new ConsultaEspecializada(data, horario, "p1", "m1", "Cardiologia", null);
        rejeitar(consulta, "Ortopedia", "incompatível");
    }

    private void rejeitar(Atendimento atendimento, String especialidadeProfissional, String mensagem) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> atendimento.validarCondicoes(especialidadeProfissional));
        assertTrue(erro.getMessage().contains(mensagem));
    }
}
