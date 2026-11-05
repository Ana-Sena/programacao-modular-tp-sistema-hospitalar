package com.example.TrabalhoSH.dominio.atendimento;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Testa somente os objetos de domínio, sem contexto Spring. */
class SituacoesAtendimentoTest {

    private final LocalDate data = LocalDate.of(2026, 11, 5);
    private final LocalTime horario = LocalTime.of(10, 30);

    private ConsultaRotina rotina() {
        return new ConsultaRotina(data, horario, "p1", "m1", "Retorno", null);
    }

    @Test
    void concluiAsDuasModalidadesAposConfirmacao() {
        Consulta especializada = new ConsultaEspecializada(data, horario, "p1", "m1", "Cardiologia", null);
        for (Consulta consulta : new Consulta[]{rotina(), especializada}) {
            assertEquals(SituacaoConsulta.AGENDADA, consulta.getSituacao());
            consulta.alterarSituacao(SituacaoConsulta.CONFIRMADA, null);
            assertEquals(SituacaoConsulta.CONFIRMADA, consulta.getSituacao());
            consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, "Cardiologia");
            assertEquals(SituacaoConsulta.EM_ATENDIMENTO, consulta.getSituacao());
            consulta.alterarSituacao(SituacaoConsulta.CONCLUIDA, null);
            assertEquals(SituacaoConsulta.CONCLUIDA, consulta.getSituacao());
        }
    }

    @Test
    void permiteCancelamentoENaoComparecimentoAntesEDepoisDaConfirmacao() {
        for (SituacaoConsulta origem : new SituacaoConsulta[]{SituacaoConsulta.AGENDADA, SituacaoConsulta.CONFIRMADA}) {
            for (SituacaoConsulta destino : new SituacaoConsulta[]{SituacaoConsulta.CANCELADA, SituacaoConsulta.NAO_COMPARECEU}) {
                Consulta consulta = rotina();
                consulta.alterarSituacao(origem, null);
                consulta.alterarSituacao(destino, null);
                assertEquals(destino, consulta.getSituacao());
            }
        }
    }

    @Test
    void rejeitaSituacaoNulaEPulosNoFluxo() {
        Consulta consulta = rotina();
        assertThrows(IllegalArgumentException.class, () -> consulta.alterarSituacao(null, null));
        assertThrows(IllegalStateException.class, () -> consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, null));
        assertThrows(IllegalStateException.class, () -> consulta.alterarSituacao(SituacaoConsulta.CONCLUIDA, null));
        assertEquals(SituacaoConsulta.AGENDADA, consulta.getSituacao());
        consulta.alterarSituacao(SituacaoConsulta.CONFIRMADA, null);
        assertThrows(IllegalStateException.class, () -> consulta.alterarSituacao(SituacaoConsulta.AGENDADA, null));
        assertThrows(IllegalStateException.class, () -> consulta.alterarSituacao(SituacaoConsulta.CONCLUIDA, null));
        assertEquals(SituacaoConsulta.CONFIRMADA, consulta.getSituacao());
        consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, null);
        for (SituacaoConsulta destino : new SituacaoConsulta[]{SituacaoConsulta.AGENDADA,
                SituacaoConsulta.CONFIRMADA, SituacaoConsulta.CANCELADA, SituacaoConsulta.NAO_COMPARECEU}) {
            assertThrows(IllegalStateException.class, () -> consulta.alterarSituacao(destino, null));
            assertEquals(SituacaoConsulta.EM_ATENDIMENTO, consulta.getSituacao());
        }
    }

    @Test
    void situacoesFinaisSoAceitamRepetirASituacaoAtual() {
        for (SituacaoConsulta fim : new SituacaoConsulta[]{SituacaoConsulta.CONCLUIDA,
                SituacaoConsulta.CANCELADA, SituacaoConsulta.NAO_COMPARECEU}) {
            Consulta consulta = rotina();
            if (fim == SituacaoConsulta.CONCLUIDA) {
                consulta.alterarSituacao(SituacaoConsulta.CONFIRMADA, null);
                consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, null);
            }
            consulta.alterarSituacao(fim, null);
            for (SituacaoConsulta destino : SituacaoConsulta.values()) {
                if (destino == fim) {
                    assertDoesNotThrow(() -> consulta.alterarSituacao(destino, null));
                } else {
                    assertThrows(IllegalStateException.class, () -> consulta.alterarSituacao(destino, null));
                }
                assertEquals(fim, consulta.getSituacao());
            }
        }
    }

    @Test
    void repetirSituacaoNaoRevalidaNemModificaConsulta() {
        Consulta consulta = new ConsultaEspecializada(data, horario, "p1", "m1", "Cardiologia", null);
        consulta.alterarSituacao(SituacaoConsulta.AGENDADA, null);
        consulta.alterarSituacao(SituacaoConsulta.CONFIRMADA, null);
        consulta.alterarSituacao(SituacaoConsulta.CONFIRMADA, null);
        consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, "Cardiologia");
        assertDoesNotThrow(() -> consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, null));
        assertEquals(SituacaoConsulta.EM_ATENDIMENTO, consulta.getSituacao());
    }

    @Test
    void falhaDeValidacaoPreservaConsultaConfirmada() {
        Consulta[] invalidas = {
            new ConsultaRotina(data, horario, "p1", "m1", " ", null),
            new ConsultaRotina(null, horario, "p1", "m1", "Retorno", null),
            new ConsultaEspecializada(data, horario, "p1", "m1", "Cardiologia", null)
        };
        for (Consulta consulta : invalidas) {
            consulta.alterarSituacao(SituacaoConsulta.CONFIRMADA, null);
            assertThrows(IllegalArgumentException.class,
                    () -> consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, "Ortopedia"));
            assertEquals(SituacaoConsulta.CONFIRMADA, consulta.getSituacao());
        }
        Consulta especializada = invalidas[2];
        assertThrows(IllegalArgumentException.class,
                () -> especializada.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, null));
        assertEquals(SituacaoConsulta.CONFIRMADA, especializada.getSituacao());
        especializada.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO, "Cardiologia");
        assertEquals(SituacaoConsulta.EM_ATENDIMENTO, especializada.getSituacao());
    }

    @Test
    void emergenciaIniciaDiretamenteEConcluiSemReativacao() {
        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(data, horario, "p1", "m1", PrioridadeEmergencia.CRITICA);
        assertEquals(SituacaoEmergencia.AGUARDANDO_AVALIACAO, emergencia.getSituacao());
        assertThrows(IllegalStateException.class, emergencia::concluirAtendimento);
        assertEquals(SituacaoEmergencia.AGUARDANDO_AVALIACAO, emergencia.getSituacao());
        emergencia.iniciarAtendimento();
        emergencia.iniciarAtendimento();
        assertEquals(SituacaoEmergencia.EM_ATENDIMENTO, emergencia.getSituacao());
        emergencia.concluirAtendimento();
        emergencia.concluirAtendimento();
        assertEquals(SituacaoEmergencia.CONCLUIDA, emergencia.getSituacao());
        assertThrows(IllegalStateException.class, emergencia::iniciarAtendimento);
        assertEquals(SituacaoEmergencia.CONCLUIDA, emergencia.getSituacao());
    }

    @Test
    void emergenciaInvalidaPermaneceAguardandoAvaliacao() {
        AtendimentoEmergencia[] invalidas = {
            new AtendimentoEmergencia(data, horario, "p1", "m1", null),
            new AtendimentoEmergencia(data, horario, "p1", "0", PrioridadeEmergencia.ALTA)
        };
        for (AtendimentoEmergencia emergencia : invalidas) {
            assertThrows(IllegalArgumentException.class, emergencia::iniciarAtendimento);
            assertEquals(SituacaoEmergencia.AGUARDANDO_AVALIACAO, emergencia.getSituacao());
        }
    }
}
