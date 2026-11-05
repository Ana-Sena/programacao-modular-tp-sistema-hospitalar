package com.example.TrabalhoSH.api.hospital;

import com.example.TrabalhoSH.api.internacao.CadastroInternacao;
import com.example.TrabalhoSH.api.internacao.InternacaoService;
import com.example.TrabalhoSH.api.paciente.CadastroPaciente;
import com.example.TrabalhoSH.api.paciente.PacienteService;
import com.example.TrabalhoSH.api.quarto.CadastroQuarto;
import com.example.TrabalhoSH.api.quarto.QuartoService;
import com.example.TrabalhoSH.dominio.internacao.TipoQuarto;
import com.example.TrabalhoSH.dominio.paciente.Endereco;
import com.example.TrabalhoSH.service.ProfissionalSaudeService;
import com.example.TrabalhoSH.controller.ProfissionalSaudeRequest;
import java.time.LocalDate;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConcorrenciaInternacaoTest {
    private String medicoId;

    @Test
    void apenasUmaRequisicaoConsegueOcuparAUltimaVaga() throws Exception {
        PacienteService pacientes = new PacienteService();
        QuartoService quartos = new QuartoService();
        ProfissionalSaudeService profissionais = new ProfissionalSaudeService();
        medicoId = cadastrarMedico(profissionais);
        InternacaoService internacoes = new InternacaoService(pacientes, quartos, profissionais);
        String p1 = pacientes.cadastrar(paciente("52998224725")).id();
        String p2 = pacientes.cadastrar(paciente("11144477735")).id();
        String quarto = quartos.cadastrar(new CadastroQuarto("101", 1, TipoQuarto.UTI, 1)).id();
        CountDownLatch largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var primeira = executor.submit(entrada(internacoes, p1, quarto, largada));
            var segunda = executor.submit(entrada(internacoes, p2, quarto, largada));
            largada.countDown();
            int aceitas = (primeira.get(5, TimeUnit.SECONDS) ? 1 : 0)
                    + (segunda.get(5, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, aceitas);
        }
        assertEquals(1, internacoes.listar().size());
        assertEquals(1, quartos.buscar(quarto).ocupacaoAtual());
    }

    @Test
    void pacienteNaoConsegueDuasEntradasSimultaneasEmQuartosDiferentes() throws Exception {
        PacienteService pacientes = new PacienteService();
        QuartoService quartos = new QuartoService();
        ProfissionalSaudeService profissionais = new ProfissionalSaudeService();
        medicoId = cadastrarMedico(profissionais);
        InternacaoService internacoes = new InternacaoService(pacientes, quartos, profissionais);
        String paciente = pacientes.cadastrar(paciente("52998224725")).id();
        String q1 = quartos.cadastrar(new CadastroQuarto("101", 1, TipoQuarto.UTI, 1)).id();
        String q2 = quartos.cadastrar(new CadastroQuarto("102", 1, TipoQuarto.UTI, 1)).id();
        CountDownLatch largada = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var primeira = executor.submit(entrada(internacoes, paciente, q1, largada));
            var segunda = executor.submit(entrada(internacoes, paciente, q2, largada));
            largada.countDown();
            int aceitas = (primeira.get(5, TimeUnit.SECONDS) ? 1 : 0)
                    + (segunda.get(5, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, aceitas);
        }
        assertEquals(1, internacoes.listar().size());
        assertEquals(1, quartos.buscar(q1).ocupacaoAtual() + quartos.buscar(q2).ocupacaoAtual());
    }

    private Callable<Boolean> entrada(InternacaoService service, String paciente, String quarto,
                                     CountDownLatch largada) {
        return () -> {
            if (!largada.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Tempo esgotado aguardando a largada do teste.");
            }
            try {
                service.cadastrar(new CadastroInternacao(paciente, medicoId, quarto, LocalDate.now(), null, null));
                return true;
            } catch (IllegalStateException conflito) {
                return false;
            }
        };
    }

    private String cadastrarMedico(ProfissionalSaudeService profissionais) {
        ProfissionalSaudeRequest entrada = new ProfissionalSaudeRequest();
        entrada.setTipo("especialista");
        entrada.setNome("Helena");
        entrada.setRegistroProfissional("CRM-MG 123");
        entrada.setEspecialidade("Cardiologia");
        return profissionais.cadastrar(entrada).getId();
    }

    private CadastroPaciente paciente(String cpf) {
        return new CadastroPaciente("Ana", cpf, LocalDate.of(2000, 1, 1), "31999999999", "ana@exemplo.com",
                new Endereco("Rua A", "10", null, "Centro", "Belo Horizonte", "MG", "30100000"));
    }
}
