package com.example.TrabalhoSH.api.consulta;

import com.example.TrabalhoSH.dominio.atendimento.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Armazenamento temporário: todos os registros são perdidos ao reiniciar a aplicação. */
@Service
public class ConsultaService {

    private final Map<String, Atendimento> registros = new LinkedHashMap<>();

    // Sincroniza leitura e alteração para não expor estados intermediários entre requisições.
    public synchronized ConsultaResposta cadastrar(CadastroConsulta entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("Os dados do cadastro são obrigatórios.");
        }
        ModalidadeConsulta modalidade = lerEnum(ModalidadeConsulta.class, entrada.modalidade(), "Modalidade");
        PrioridadeEmergencia prioridade = entrada.prioridade() == null ? null
                : lerEnum(PrioridadeEmergencia.class, entrada.prioridade(), "Prioridade");
        Atendimento atendimento = switch (modalidade) {
            case ROTINA -> new ConsultaRotina(entrada.data(), entrada.horario(), entrada.pacienteId(),
                    entrada.medicoResponsavelId(), entrada.motivo(), entrada.observacoes());
            case ESPECIALIZADA -> new ConsultaEspecializada(entrada.data(), entrada.horario(), entrada.pacienteId(),
                    entrada.medicoResponsavelId(), entrada.especialidade(), entrada.observacoes());
            case EMERGENCIA -> new AtendimentoEmergencia(entrada.data(), entrada.horario(), entrada.pacienteId(),
                    entrada.medicoResponsavelId(), prioridade);
        };
        atendimento.validarEstrutura();
        String especialidadeProfissional = atendimento instanceof ConsultaEspecializada
                ? obterEspecialidadeProfissional(atendimento.getMedicoResponsavelId()) : null;
        atendimento.validarCondicoes(especialidadeProfissional);
        // Só publica o registro depois de todas as validações, preservando a situação do domínio.
        String id;
        do {
            id = UUID.randomUUID().toString();
        } while (registros.containsKey(id));
        ConsultaResposta resposta = ConsultaResposta.de(id, atendimento);
        registros.put(id, atendimento);
        return resposta;
    }

    public synchronized List<ConsultaResposta> listar() {
        return registros.entrySet().stream()
                .map(registro -> ConsultaResposta.de(registro.getKey(), registro.getValue())).toList();
    }

    public synchronized ConsultaResposta buscar(String id) {
        return ConsultaResposta.de(id, obter(id));
    }

    public synchronized ConsultaResposta alterarSituacao(String id, AlteracaoSituacao entrada) {
        Atendimento atendimento = obter(id);
        if (entrada == null) {
            throw new IllegalArgumentException("A nova situação é obrigatória.");
        }
        if (atendimento instanceof Consulta consulta) {
            SituacaoConsulta destino = lerEnum(SituacaoConsulta.class, entrada.situacao(), "Situação da consulta");
            String especialidadeProfissional = null;
            if (consulta instanceof ConsultaEspecializada
                    && consulta.getSituacao() == SituacaoConsulta.CONFIRMADA
                    && destino == SituacaoConsulta.EM_ATENDIMENTO) {
                especialidadeProfissional = obterEspecialidadeProfissional(consulta.getMedicoResponsavelId());
            }
            consulta.alterarSituacao(destino, especialidadeProfissional);
        } else {
            AtendimentoEmergencia emergencia = (AtendimentoEmergencia) atendimento;
            SituacaoEmergencia destino = lerEnum(SituacaoEmergencia.class, entrada.situacao(), "Situação da emergência");
            switch (destino) {
                case EM_ATENDIMENTO -> emergencia.iniciarAtendimento();
                case CONCLUIDA -> emergencia.concluirAtendimento();
                case AGUARDANDO_AVALIACAO -> {
                    if (emergencia.getSituacao() != destino) {
                        throw new IllegalStateException("Não é permitido retornar a emergência à espera por avaliação.");
                    }
                }
            }
        }
        return ConsultaResposta.de(id, atendimento);
    }

    private Atendimento obter(String id) {
        Atendimento atendimento = registros.get(id);
        if (atendimento == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de consulta não encontrado.");
        }
        return atendimento;
    }

    private String obterEspecialidadeProfissional(String medicoId) {
        // Integrar com o módulo da Alexia usando medicoId; nunca confiar nesse dado vindo do cliente.
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "Integração com profissionais indisponível: não é possível verificar a especialidade do médico responsável.");
    }

    private static <E extends Enum<E>> E lerEnum(Class<E> tipo, String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatória.");
        }
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException erro) {
            throw new IllegalArgumentException(campo + " desconhecida. Valores aceitos: "
                    + java.util.Arrays.toString(tipo.getEnumConstants()) + ".");
        }
    }
}
