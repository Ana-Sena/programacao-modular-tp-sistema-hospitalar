package com.example.TrabalhoSH.api.internacao;

import com.example.TrabalhoSH.api.paciente.PacienteService;
import com.example.TrabalhoSH.api.quarto.QuartoService;
import com.example.TrabalhoSH.dominio.internacao.Internacao;
import com.example.TrabalhoSH.dominio.internacao.Quarto;
import com.example.TrabalhoSH.dominio.internacao.SituacaoInternacao;
import com.example.TrabalhoSH.dominio.paciente.Paciente;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Registros em memória, com entradas, altas e leituras de quartos no mesmo monitor. */
@Service
public class InternacaoService {

    private final Map<String, Internacao> registros = new LinkedHashMap<>();
    private final PacienteService pacientes;
    private final QuartoService quartos;

    public InternacaoService(PacienteService pacientes, QuartoService quartos) {
        this.pacientes = pacientes;
        this.quartos = quartos;
    }

    public InternacaoResposta cadastrar(CadastroInternacao entrada) {
        synchronized (quartos) {
            if (entrada == null || entrada.pacienteId() == null || entrada.pacienteId().isBlank()
                    || entrada.quartoId() == null || entrada.quartoId().isBlank()) {
                throw new IllegalArgumentException("Os dados da internação, paciente e quarto são obrigatórios.");
            }
            Paciente paciente = pacientes.obter(entrada.pacienteId());
            Quarto quarto = quartos.obter(entrada.quartoId());
            if (registros.values().stream().anyMatch(atual -> atual.estaAtiva()
                    && atual.getPaciente().getId().equals(paciente.getId()))) {
                throw new IllegalStateException("Paciente já possui uma internação ativa.");
            }
            Internacao internacao = new Internacao(UUID.randomUUID().toString(), paciente,
                    entrada.medicoResponsavelId(), quarto, entrada.dataEntrada(),
                    entrada.dataPrevistaAlta(), entrada.observacoes());
            registros.put(internacao.getId(), internacao);
            return InternacaoResposta.de(internacao);
        }
    }

    public List<InternacaoResposta> listar() {
        synchronized (quartos) {
            return registros.values().stream().map(InternacaoResposta::de).toList();
        }
    }

    public InternacaoResposta buscar(String id) {
        synchronized (quartos) {
            return InternacaoResposta.de(obter(id));
        }
    }

    public InternacaoResposta alterarSituacao(String id, AlteracaoSituacao entrada) {
        synchronized (quartos) {
            Internacao internacao = obter(id);
            if (entrada == null || entrada.situacao() == null) {
                throw new IllegalArgumentException("A situação da internação é obrigatória.");
            }
            if (entrada.situacao() == SituacaoInternacao.ALTA) {
                internacao.registrarAlta(entrada.dataEfetivaAlta());
            } else {
                if (entrada.dataEfetivaAlta() != null) {
                    throw new IllegalArgumentException("Uma internação ativa não pode possuir data efetiva de alta.");
                }
                if (!internacao.estaAtiva()) {
                    throw new IllegalStateException("Não é permitido reabrir uma internação após a alta.");
                }
            }
            return InternacaoResposta.de(internacao);
        }
    }

    private Internacao obter(String id) {
        Internacao internacao = registros.get(id);
        if (internacao == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Internação não encontrada.");
        }
        return internacao;
    }
}
