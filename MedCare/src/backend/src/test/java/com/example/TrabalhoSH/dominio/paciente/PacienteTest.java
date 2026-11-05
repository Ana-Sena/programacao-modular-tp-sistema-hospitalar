package com.example.TrabalhoSH.dominio.paciente;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PacienteTest {

    private final Endereco endereco = new Endereco("Rua A", "10", null, "Centro",
            "Belo Horizonte", "mg", "30100-000");

    @Test
    void normalizaCpfEEndereco() {
        Paciente paciente = novoPaciente("529.982.247-25");
        assertEquals("52998224725", paciente.getCpf());
        assertEquals("MG", endereco.uf());
        assertEquals("30100000", endereco.cep());
        assertEquals("Rua A, 10 - Centro, Belo Horizonte/MG - CEP 30100000", endereco.enderecoCompleto());
    }

    @Test
    void rejeitaCpfComDigitosInvalidosOuFormatoInvalido() {
        for (String cpf : new String[]{"11111111111", "52998224724", "5299822472", "abc52998224725"}) {
            assertThrows(IllegalArgumentException.class, () -> novoPaciente(cpf));
        }
    }

    @Test
    void atualizacaoInvalidaPreservaCadastro() {
        Paciente paciente = novoPaciente("52998224725");
        assertThrows(IllegalArgumentException.class, () -> paciente.atualizar("Outro nome",
                LocalDate.of(2000, 1, 1), "123", "invalido", endereco));
        assertEquals("Ana", paciente.getNome());
        assertThrows(IllegalArgumentException.class, () -> paciente.atualizar("Ana",
                LocalDate.now().plusDays(1), "123", "ana@exemplo.com", endereco));
        assertThrows(IllegalArgumentException.class, () -> new Endereco("Rua", "1", null,
                "Centro", "Cidade", "XX", "30100000"));
        assertThrows(IllegalArgumentException.class, () -> new Endereco("Rua", "1", null,
                "Centro", "Cidade", "MG", "123"));
    }

    private Paciente novoPaciente(String cpf) {
        return new Paciente("p1", "Ana", cpf, LocalDate.of(2000, 1, 1), "31999999999",
                "ana@exemplo.com", endereco);
    }
}
