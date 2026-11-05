package com.example.TrabalhoSH.api.consulta;

import com.example.TrabalhoSH.service.ProfissionalSaudeService;
import com.example.TrabalhoSH.controller.ProfissionalSaudeRequest;
import com.example.TrabalhoSH.controller.ProfissionalSaudeController;
import com.example.TrabalhoSH.api.hospital.HospitalErros;
import com.example.TrabalhoSH.api.paciente.*;
import com.example.TrabalhoSH.dominio.paciente.Endereco;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Controller e serviço reais, com MockMvc isolado: não inicia aplicação ou servidor. */
class ConsultaApiTest {

    private ConsultaService service;
    private String pacienteId;
    private String medicoId;
    private ProfissionalSaudeService profissionais;
    private MockMvc mvc;
    private static final String ROTINA = """
            {"modalidade":"ROTINA","data":"2026-11-05","horario":"10:30:00",
             "pacienteId":"p1","medicoResponsavelId":"m1","motivo":"Retorno"}
            """;
    private static final String EMERGENCIA = """
            {"modalidade":"EMERGENCIA","data":"2026-11-05","horario":"10:30:00",
             "pacienteId":"p1","medicoResponsavelId":"m1","prioridade":"CRITICA"}
            """;
    private static final String ESPECIALIZADA = """
            {"modalidade":"ESPECIALIZADA","data":"2026-11-05","horario":"10:30:00",
             "pacienteId":"p1","medicoResponsavelId":"m1","especialidade":"Cardiologia"}
            """;

    @BeforeEach
    void preparar() {
        PacienteService pacientes = new PacienteService();
        profissionais = new ProfissionalSaudeService();
        medicoId = cadastrarMedico(profissionais);
        pacienteId = pacientes.cadastrar(new CadastroPaciente("Ana", "52998224725", LocalDate.of(2000, 1, 1),
                "31999999999", "ana@exemplo.com", new Endereco("Rua A", "10", null,
                "Centro", "Belo Horizonte", "MG", "30100000"))).id();
        service = new ConsultaService(pacientes, profissionais);
        mvc = MockMvcBuilders.standaloneSetup(new ConsultaController(service), new ProfissionalSaudeController(profissionais))
                .setControllerAdvice(new ConsultaErros(), new HospitalErros()).build();
    }

    @Test
    void listaVaziaCadastraBuscaEProduzIdsUnicos() throws Exception {
        mvc.perform(get("/consultas")).andExpect(status().isOk()).andExpect(content().json("[]"));
        String id = cadastrar(ROTINA);
        mvc.perform(get("/consultas/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id)).andExpect(jsonPath("$.modalidade").value("ROTINA"))
                .andExpect(jsonPath("$.situacao").value("AGENDADA"))
                .andExpect(jsonPath("$.motivo").value("Retorno"))
                .andExpect(jsonPath("$.data").value("2026-11-05"));
        String outro = cadastrar(ROTINA);
        assertNotEquals(id, outro);
        mvc.perform(get("/consultas")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void retorna404ParaRegistroInexistente() throws Exception {
        mvc.perform(get("/consultas/inexistente")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Registro de consulta não encontrado."));
        mvc.perform(patch("/consultas/inexistente/situacao").contentType(MediaType.APPLICATION_JSON)
                .content("{\"situacao\":\"CONFIRMADA\"}")).andExpect(status().isNotFound());
    }

    @Test
    void rejeitaEntradasInvalidasSemSalvarRegistros() throws Exception {
        String[] invalidos = {"{}", "null", "{", ROTINA.replace("ROTINA", "CIRURGIA"),
                ROTINA.replace("2026-11-05", "data-invalida"), ROTINA.replace("10:30:00", "25:00"),
                ROTINA.replace("\"p1\"", "\"0\""), ROTINA.replace("\"m1\"", "\"\""),
                ROTINA.replace("\"Retorno\"", "null"), EMERGENCIA.replace("CRITICA", "URGENTE"),
                EMERGENCIA.replace("\"CRITICA\"", "null")};
        for (String json : invalidos) {
            mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(associar(json)))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
        }
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void rejeitaIdSituacaoEEspecialidadeDoProfissionalNoCadastro() throws Exception {
        for (String extra : new String[]{"\"id\":\"escolhido\"", "\"situacao\":\"CONCLUIDA\"",
                "\"especialidadeProfissional\":\"Cardiologia\""}) {
            mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                    .content(ROTINA.replace("}", "," + extra + "}")))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensagem").isNotEmpty());
        }
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void especializadaUsaCadastroRealDoProfissional() throws Exception {
        String id = cadastrar(ESPECIALIZADA);
        alterar(id, "CONFIRMADA", 200);
        alterar(id, "EM_ATENDIMENTO", 200);
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(associar(ESPECIALIZADA.replace("Cardiologia", "Ortopedia"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(associar(ESPECIALIZADA.replace("}", ",\"especialidadeProfissional\":\"Cardiologia\"}"))))
                .andExpect(status().isBadRequest());
        assertEquals(1, service.listar().size());
    }

    @Test
    void profissionalCadastradoPelaApiPodeAtenderConsultaEspecializada() throws Exception {
        mvc.perform(post("/profissionais").contentType(MediaType.APPLICATION_JSON).content("""
                {"tipo":"especialista","nome":"Maria","registroProfissional":"CRM-MG 456",
                 "especialidade":"Ortopedia","telefone":"31999999999","email":"maria@exemplo.com"}
                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty());
        String medico = profissionais.listar().get(1).getId();
        String id = cadastrar(ESPECIALIZADA.replace("m1", medico).replace("Cardiologia", "Ortopedia"));
        alterar(id, "CONFIRMADA", 200);
        // O início consulta novamente o cadastro, sem usar a especialidade enviada pelo cliente.
        profissionais.obter(medico).setEspecialidade("Cardiologia");
        alterar(id, "EM_ATENDIMENTO", 400);
        assertEquals("CONFIRMADA", service.buscar(id).situacao());
        profissionais.obter(medico).setEspecialidade("Ortopedia");
        alterar(id, "EM_ATENDIMENTO", 200);
    }

    @Test
    void rejeitaPacienteOuMedicoNaoCadastrado() throws Exception {
        for (String json : new String[]{ROTINA.replace("p1", "inexistente"), ROTINA.replace("m1", "inexistente")}) {
            mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(associar(json)))
                    .andExpect(status().isNotFound());
        }
        assertTrue(service.listar().isEmpty());
    }

    @Test
    void rotinaRespeitaFluxoERejeitaMudancasSemAlterarRegistro() throws Exception {
        String id = cadastrar(ROTINA);
        alterar(id, "CONCLUIDA", 409);
        assertEquals("AGENDADA", service.buscar(id).situacao());
        alterar(id, "DESCONHECIDA", 400);
        mvc.perform(patch("/consultas/{id}/situacao", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"situacao\":null}")).andExpect(status().isBadRequest());
        assertEquals("AGENDADA", service.buscar(id).situacao());
        alterar(id, "CONFIRMADA", 200);
        alterar(id, "EM_ATENDIMENTO", 200);
        alterar(id, "EM_ATENDIMENTO", 200);
        alterar(id, "CONCLUIDA", 200);
        alterar(id, "AGENDADA", 409);
        assertEquals("CONCLUIDA", service.buscar(id).situacao());
    }

    @Test
    void cancelamentoENaoComparecimentoMantemRegistroFinal() throws Exception {
        for (String situacao : new String[]{"CANCELADA", "NAO_COMPARECEU"}) {
            String id = cadastrar(ROTINA);
            alterar(id, situacao, 200);
            alterar(id, "CONFIRMADA", 409);
            alterar(id, "CONCLUIDA", 409);
            assertEquals(situacao, service.buscar(id).situacao());
        }
        assertEquals(2, service.listar().size());
    }

    @Test
    void emergenciaIniciaSemConfirmacaoEPreservaSituacaoEmFalhas() throws Exception {
        String id = cadastrar(EMERGENCIA);
        assertEquals("AGUARDANDO_AVALIACAO", service.buscar(id).situacao());
        alterar(id, "AGUARDANDO_AVALIACAO", 200);
        alterar(id, "CONCLUIDA", 409);
        assertEquals("AGUARDANDO_AVALIACAO", service.buscar(id).situacao());
        alterar(id, "EM_ATENDIMENTO", 200);
        alterar(id, "AGUARDANDO_AVALIACAO", 409);
        assertEquals("EM_ATENDIMENTO", service.buscar(id).situacao());
        alterar(id, "CONCLUIDA", 200);
        alterar(id, "CONCLUIDA", 200);
        alterar(id, "EM_ATENDIMENTO", 409);
        assertEquals("CONCLUIDA", service.buscar(id).situacao());
    }

    private String cadastrarMedico(ProfissionalSaudeService profissionais) {
        ProfissionalSaudeRequest entrada = new ProfissionalSaudeRequest();
        entrada.setTipo("especialista");
        entrada.setNome("Helena");
        entrada.setRegistroProfissional("CRM-MG 123");
        entrada.setEspecialidade("Cardiologia");
        return profissionais.cadastrar(entrada).getId();
    }

    private String associar(String json) {
        return json.replace("p1", pacienteId).replace("m1", medicoId);
    }

    private String cadastrar(String json) throws Exception {
        var resultado = mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(associar(json)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty()).andReturn();
        String local = resultado.getResponse().getHeader("Location");
        assertNotNull(local);
        String id = local.substring(local.lastIndexOf('/') + 1);
        assertEquals("http://localhost/consultas/" + id, local);
        assertEquals(id, service.buscar(id).id());
        return id;
    }

    private void alterar(String id, String situacao, int statusEsperado) throws Exception {
        var resultado = mvc.perform(patch("/consultas/{id}/situacao", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"situacao\":\"" + situacao + "\"}"))
                .andExpect(status().is(statusEsperado));
        if (statusEsperado == 200) {
            resultado.andExpect(jsonPath("$.situacao").value(situacao));
        } else {
            resultado.andExpect(jsonPath("$.mensagem").isNotEmpty());
        }
    }
}
