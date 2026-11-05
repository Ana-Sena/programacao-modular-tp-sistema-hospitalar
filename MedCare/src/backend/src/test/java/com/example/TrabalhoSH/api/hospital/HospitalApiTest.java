package com.example.TrabalhoSH.api.hospital;

import com.example.TrabalhoSH.api.internacao.InternacaoController;
import com.example.TrabalhoSH.api.internacao.InternacaoService;
import com.example.TrabalhoSH.api.paciente.PacienteController;
import com.example.TrabalhoSH.api.paciente.PacienteService;
import com.example.TrabalhoSH.api.quarto.QuartoController;
import com.example.TrabalhoSH.api.quarto.QuartoService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Controllers e serviços reais; valida o contrato HTTP sem iniciar servidor. */
class HospitalApiTest {

    private static final String PACIENTE = """
            {"nome":"Ana","cpf":"529.982.247-25","dataNascimento":"2000-01-01",
             "telefone":"31999999999","email":"ana@exemplo.com",
             "endereco":{"logradouro":"Rua A","numero":"10","bairro":"Centro",
             "cidade":"Belo Horizonte","uf":"MG","cep":"30100-000"}}
            """;
    private static final String QUARTO = """
            {"numero":"101","andar":1,"tipo":"APARTAMENTO","capacidadeMaxima":1}
            """;

    private MockMvc mvc;
    private PacienteService pacientes;
    private QuartoService quartos;
    private InternacaoService internacoes;

    @BeforeEach
    void preparar() {
        pacientes = new PacienteService();
        quartos = new QuartoService();
        internacoes = new InternacaoService(pacientes, quartos);
        mvc = MockMvcBuilders.standaloneSetup(new PacienteController(pacientes),
                new QuartoController(quartos), new InternacaoController(internacoes))
                .setControllerAdvice(new HospitalErros()).build();
    }

    @Test
    void cadastraListaEBuscaPacientesEQuartos() throws Exception {
        for (String recurso : new String[]{"pacientes", "quartos", "internacoes"}) {
            mvc.perform(get("/" + recurso)).andExpect(status().isOk()).andExpect(content().json("[]"));
        }
        String pacienteId = cadastrar("pacientes", PACIENTE);
        String quartoId = cadastrar("quartos", QUARTO);
        mvc.perform(get("/pacientes/{id}", pacienteId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pacienteId)).andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.endereco.cep").value("30100000"));
        mvc.perform(get("/quartos/{id}", quartoId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(quartoId)).andExpect(jsonPath("$.ocupacaoAtual").value(0))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"));
        mvc.perform(get("/pacientes")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/quartos")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void impedeCpfENumeroDeQuartoDuplicados() throws Exception {
        cadastrar("pacientes", PACIENTE);
        cadastrar("quartos", QUARTO);
        rejeitarCadastro("pacientes", PACIENTE.replace("529.982.247-25", "52998224725"), 409);
        rejeitarCadastro("quartos", QUARTO.replace("101", " 101 "), 409);
        assertEquals(1, pacientes.listar().size());
        assertEquals(1, quartos.listar().size());
    }

    @Test
    void rejeitaCadastroInvalidoDePacienteSemPublicarDados() throws Exception {
        for (String json : new String[]{"{}", "null", "{", PACIENTE.replace("529.982.247-25", "11111111111"),
                PACIENTE.replace("ana@exemplo.com", "email-invalido"), PACIENTE.replace("MG", "XX"),
                PACIENTE.replace("30100-000", "123"), PACIENTE.replace("2000-01-01", "2999-01-01"),
                PACIENTE.replace("2000-01-01", "data-invalida"), PACIENTE.replace("\"nome\":\"Ana\"", "\"nome\":\" \""),
                PACIENTE.replace("{\"nome\"", "{\"id\":\"forjado\",\"nome\"")}) {
            rejeitarCadastro("pacientes", json, 400);
        }
        assertTrue(pacientes.listar().isEmpty());
    }

    @Test
    void atualizaPacienteEPreservaDadosEmFalha() throws Exception {
        String id = cadastrar("pacientes", PACIENTE);
        String atualizacao = """
                {"nome":"Ana Sena","telefone":"3133333333","email":"novo@exemplo.com",
                 "endereco":{"logradouro":"Rua B","numero":"20","bairro":"Centro",
                 "cidade":"Belo Horizonte","uf":"MG","cep":"30100000"}}
                """;
        mvc.perform(put("/pacientes/{id}", id).contentType(MediaType.APPLICATION_JSON).content(atualizacao))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Ana Sena"))
                .andExpect(jsonPath("$.cpf").value("52998224725"));
        mvc.perform(put("/pacientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(atualizacao.replace("novo@exemplo.com", "invalido").replace("Ana Sena", "Outro nome")))
                .andExpect(status().isBadRequest());
        assertEquals("Ana Sena", pacientes.buscar(id).nome());
        mvc.perform(put("/pacientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(atualizacao.replace("{\"nome\"", "{\"cpf\":\"11111111111\",\"nome\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejeitaQuartosInvalidosECamposDeOcupacaoControladosPeloServidor() throws Exception {
        for (String json : new String[]{"{}", "null", "{", QUARTO.replace("APARTAMENTO", "INVALIDO"),
                QUARTO.replace("\"tipo\":\"APARTAMENTO\"", "\"tipo\":null"),
                QUARTO.replace("\"andar\":1,", ""), QUARTO.replace("\"capacidadeMaxima\":1", "\"capacidadeMaxima\":0"),
                QUARTO.replace("\"capacidadeMaxima\":1", "\"capacidadeMaxima\":2"),
                QUARTO.replace("\"numero\":\"101\"", "\"numero\":\"\""),
                QUARTO.replace("}", ",\"ocupacaoAtual\":1}"), QUARTO.replace("}", ",\"situacao\":\"OCUPADO\"}")}) {
            rejeitarCadastro("quartos", json, 400);
        }
        assertTrue(quartos.listar().isEmpty());
    }

    @Test
    void entradaEAltaAtualizamAssociacoesESituacoesSemApagarHistorico() throws Exception {
        String pacienteId = cadastrar("pacientes", PACIENTE);
        String quartoId = cadastrar("quartos", QUARTO);
        String id = cadastrar("internacoes", entrada(pacienteId, quartoId));
        mvc.perform(get("/internacoes/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId").value(pacienteId)).andExpect(jsonPath("$.quartoId").value(quartoId))
                .andExpect(jsonPath("$.situacao").value("ATIVA"));
        assertEquals(1, quartos.buscar(quartoId).ocupacaoAtual());
        situacao(id, "{\"situacao\":\"ATIVA\"}", 200);
        String alta = "{\"situacao\":\"ALTA\",\"dataEfetivaAlta\":\"" + LocalDate.now() + "\"}";
        situacao(id, alta, 200);
        situacao(id, alta, 200);
        situacao(id, "{\"situacao\":\"ATIVA\"}", 409);
        assertEquals(0, quartos.buscar(quartoId).ocupacaoAtual());
        assertEquals(LocalDate.now(), internacoes.buscar(id).dataEfetivaAlta());
        assertEquals("ALTA", internacoes.buscar(id).situacao().name());
        String nova = cadastrar("internacoes", entrada(pacienteId, quartoId));
        assertNotEquals(id, nova);
        assertEquals(2, internacoes.listar().size());
        // Repetir a alta antiga não libera a vaga da nova internação.
        situacao(id, alta, 200);
        assertEquals(1, quartos.buscar(quartoId).ocupacaoAtual());
    }

    @Test
    void pacienteNaoPodeTerDuasInternacoesAtivasEQuartoNaoPodeExcederCapacidade() throws Exception {
        String pacienteId = cadastrar("pacientes", PACIENTE);
        String outroPaciente = cadastrar("pacientes", PACIENTE.replace("529.982.247-25", "11144477735"));
        String quartoId = cadastrar("quartos", QUARTO);
        String outroQuarto = cadastrar("quartos", QUARTO.replace("101", "102"));
        cadastrar("internacoes", entrada(pacienteId, quartoId));
        rejeitarCadastro("internacoes", entrada(pacienteId, outroQuarto), 409);
        rejeitarCadastro("internacoes", entrada(outroPaciente, quartoId), 409);
        assertEquals(0, quartos.buscar(outroQuarto).ocupacaoAtual());
        assertEquals(1, quartos.buscar(quartoId).ocupacaoAtual());
        assertEquals(1, internacoes.listar().size());
    }

    @Test
    void enfermariaMantemVagasAteAtingirCapacidade() throws Exception {
        String p1 = cadastrar("pacientes", PACIENTE);
        String p2 = cadastrar("pacientes", PACIENTE.replace("529.982.247-25", "11144477735"));
        String quarto = cadastrar("quartos", QUARTO.replace("APARTAMENTO", "ENFERMARIA")
                .replace("\"capacidadeMaxima\":1", "\"capacidadeMaxima\":2"));
        String primeira = cadastrar("internacoes", entrada(p1, quarto));
        assertEquals("DISPONIVEL", quartos.buscar(quarto).situacao().name());
        cadastrar("internacoes", entrada(p2, quarto));
        assertEquals("OCUPADO", quartos.buscar(quarto).situacao().name());
        situacao(primeira, "{\"situacao\":\"ALTA\",\"dataEfetivaAlta\":\"" + LocalDate.now() + "\"}", 200);
        assertEquals(1, quartos.buscar(quarto).ocupacaoAtual());
        assertEquals("DISPONIVEL", quartos.buscar(quarto).situacao().name());
    }

    @Test
    void rejeitaEntradasInvalidasSemOcuparVagas() throws Exception {
        String paciente = cadastrar("pacientes", PACIENTE);
        String quarto = cadastrar("quartos", QUARTO);
        String entrada = entrada(paciente, quarto);
        for (String json : new String[]{"{}", "null", "{", entrada.replace("\"m1\"", "\" \""),
                entrada.replace(LocalDate.now().toString(), LocalDate.now().plusDays(1).toString()),
                entrada.replace(LocalDate.now().toString(), "1999-01-01"),
                entrada.replace("}", ",\"dataPrevistaAlta\":\"1999-01-01\"}"),
                entrada.replace("}", ",\"situacao\":\"ALTA\"}"),
                entrada.replace("}", ",\"dataEfetivaAlta\":\"2020-01-01\"}"),
                entrada.replace("\"pacienteId\":\"" + paciente + "\"", "\"pacienteId\":\"\"")}) {
            rejeitarCadastro("internacoes", json, 400);
        }
        rejeitarCadastro("internacoes", entrada.replace(paciente, "inexistente"), 404);
        rejeitarCadastro("internacoes", entrada.replace(quarto, "inexistente"), 404);
        assertTrue(internacoes.listar().isEmpty());
        assertEquals(0, quartos.buscar(quarto).ocupacaoAtual());
    }

    @Test
    void altaInvalidaPreservaSituacaoEVaga() throws Exception {
        String paciente = cadastrar("pacientes", PACIENTE);
        String quarto = cadastrar("quartos", QUARTO);
        String id = cadastrar("internacoes", entrada(paciente, quarto));
        for (String json : new String[]{"{}", "null", "{", "{\"situacao\":\"DESCONHECIDA\"}",
                "{\"situacao\":\"ALTA\"}", "{\"situacao\":\"ALTA\",\"dataEfetivaAlta\":\"1999-01-01\"}",
                "{\"situacao\":\"ALTA\",\"dataEfetivaAlta\":\"2999-01-01\"}",
                "{\"situacao\":\"ATIVA\",\"dataEfetivaAlta\":\"" + LocalDate.now() + "\"}",
                "{\"situacao\":\"ALTA\",\"quartoId\":\"outro\"}"}) {
            situacao(id, json, 400);
        }
        assertEquals("ATIVA", internacoes.buscar(id).situacao().name());
        assertNull(internacoes.buscar(id).dataEfetivaAlta());
        assertEquals(1, quartos.buscar(quarto).ocupacaoAtual());
    }

    @Test
    void recursosInexistentesRetornam404() throws Exception {
        for (String recurso : new String[]{"pacientes", "quartos", "internacoes"}) {
            mvc.perform(get("/" + recurso + "/inexistente")).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.mensagem").isNotEmpty());
        }
        situacao("inexistente", "{\"situacao\":\"ATIVA\"}", 404);
        mvc.perform(put("/pacientes/inexistente").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    private String cadastrar(String recurso, String json) throws Exception {
        var resultado = mvc.perform(post("/" + recurso).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty()).andReturn();
        String local = resultado.getResponse().getHeader("Location");
        assertNotNull(local);
        String id = local.substring(local.lastIndexOf('/') + 1);
        assertEquals("http://localhost/" + recurso + "/" + id, local);
        return id;
    }

    private void rejeitarCadastro(String recurso, String json, int statusEsperado) throws Exception {
        mvc.perform(post("/" + recurso).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(statusEsperado)).andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    private void situacao(String id, String json, int statusEsperado) throws Exception {
        mvc.perform(patch("/internacoes/{id}/situacao", id).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(statusEsperado));
    }

    private String entrada(String pacienteId, String quartoId) {
        return "{\"pacienteId\":\"" + pacienteId + "\",\"quartoId\":\"" + quartoId
                + "\",\"medicoResponsavelId\":\"m1\",\"dataEntrada\":\"" + LocalDate.now() + "\"}";
    }
}
