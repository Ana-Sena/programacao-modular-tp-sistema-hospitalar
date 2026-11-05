package com.example.TrabalhoSH.dominio.internacao;

import com.example.TrabalhoSH.dominio.paciente.Endereco;
import com.example.TrabalhoSH.dominio.paciente.Paciente;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InternacaoTest {

    private final LocalDate hoje = LocalDate.now();
    private final Paciente paciente = new Paciente("p1", "Ana", "52998224725",
            LocalDate.of(2000, 1, 1), "31999999999", "ana@exemplo.com",
            new Endereco("Rua A", "10", null, "Centro", "Belo Horizonte", "MG", "30100000"));

    @Test
    void controlaCapacidadeEAltaSemLiberacaoDuplicada() {
        Quarto quarto = new Quarto("q1", "101", 1, TipoQuarto.APARTAMENTO, 1);
        Internacao internacao = internar(quarto, hoje);
        assertSame(paciente, internacao.getPaciente());
        assertSame(quarto, internacao.getQuarto());
        assertEquals(SituacaoQuarto.OCUPADO, quarto.getSituacao());
        assertThrows(IllegalStateException.class, () -> internar(quarto, hoje));
        assertEquals(1, quarto.getOcupacaoAtual());
        internacao.registrarAlta(hoje);
        internacao.registrarAlta(hoje);
        assertFalse(internacao.estaAtiva());
        assertEquals(hoje, internacao.getDataEfetivaAlta());
        assertEquals(0, quarto.getOcupacaoAtual());
        assertEquals(SituacaoQuarto.DISPONIVEL, quarto.getSituacao());
    }

    @Test
    void validaTiposEVagasParciaisDaEnfermaria() {
        for (TipoQuarto tipo : TipoQuarto.values()) {
            assertThrows(IllegalArgumentException.class, () -> new Quarto("q", "1", 0, tipo, 0));
        }
        assertThrows(IllegalArgumentException.class, () -> new Quarto("q", "1", 0, TipoQuarto.UTI, 2));
        assertThrows(IllegalArgumentException.class, () -> new Quarto("q", "1", 0, TipoQuarto.APARTAMENTO, 2));
        Quarto quarto = new Quarto("q1", "101", 1, TipoQuarto.ENFERMARIA, 2);
        internar(quarto, hoje);
        assertTrue(quarto.possuiVaga());
        assertEquals(SituacaoQuarto.DISPONIVEL, quarto.getSituacao());
        assertThrows(IllegalStateException.class, () -> new Quarto("q", "1", 0, TipoQuarto.UTI, 1).liberar());
    }

    @Test
    void datasInvalidasNaoOcupamOuLiberamVaga() {
        Quarto quarto = new Quarto("q1", "101", 1, TipoQuarto.UTI, 1);
        assertThrows(IllegalArgumentException.class, () -> internar(quarto, hoje.plusDays(1)));
        assertThrows(IllegalArgumentException.class, () -> new Internacao("i", paciente, "m1", quarto,
                hoje, hoje.minusDays(1), null));
        assertEquals(0, quarto.getOcupacaoAtual());
        Internacao internacao = internar(quarto, hoje.minusDays(2));
        assertThrows(IllegalArgumentException.class, () -> internacao.registrarAlta(hoje.minusDays(3)));
        assertThrows(IllegalArgumentException.class, () -> internacao.registrarAlta(hoje.plusDays(1)));
        assertTrue(internacao.estaAtiva());
        assertEquals(1, quarto.getOcupacaoAtual());
        internacao.registrarAlta(hoje);
        assertThrows(IllegalStateException.class, () -> internacao.registrarAlta(hoje.minusDays(1)));
        assertEquals(0, quarto.getOcupacaoAtual());
    }

    private Internacao internar(Quarto quarto, LocalDate entrada) {
        return new Internacao("i1", paciente, "m1", quarto, entrada, null, "Observação");
    }
}
