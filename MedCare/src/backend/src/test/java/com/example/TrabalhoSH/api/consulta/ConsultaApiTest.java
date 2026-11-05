package com.example.TrabalhoSH.api.consulta;

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
        service = new ConsultaService();
        mvc = MockMvcBuilders.standaloneSetup(new ConsultaController(service))
                .setControllerAdvice(new ConsultaErros()).build();
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
            mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(json))
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
    void especializadaExigeIntegracaoConfiavelESemCadastroParcial() throws Exception {
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(ESPECIALIZADA))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensagem").value("Integração com profissionais indisponível: não é possível verificar a especialidade do médico responsável."));
        mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(ESPECIALIZADA.replace("}", ",\"especialidadeProfissional\":\"Cardiologia\"}")))
                .andExpect(status().isBadRequest());
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

    private String cadastrar(String json) throws Exception {
        var resultado = mvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON).content(json))
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
