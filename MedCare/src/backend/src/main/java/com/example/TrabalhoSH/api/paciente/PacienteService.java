package com.example.TrabalhoSH.api.paciente;

import com.example.TrabalhoSH.dominio.paciente.Paciente;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Armazenamento temporário: os cadastros são perdidos ao reiniciar a aplicação. */
@Service
public class PacienteService {

    private final Map<String, Paciente> registros = new LinkedHashMap<>();

    public synchronized PacienteResposta cadastrar(CadastroPaciente entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("Os dados do paciente são obrigatórios.");
        }
        Paciente paciente = new Paciente(UUID.randomUUID().toString(), entrada.nome(), entrada.cpf(),
                entrada.dataNascimento(), entrada.telefone(), entrada.email(), entrada.endereco());
        if (registros.values().stream().anyMatch(atual -> atual.getCpf().equals(paciente.getCpf()))) {
            throw new IllegalStateException("Já existe paciente com este CPF.");
        }
        registros.put(paciente.getId(), paciente);
        return PacienteResposta.de(paciente);
    }

    public synchronized List<PacienteResposta> listar() {
        return registros.values().stream().map(PacienteResposta::de).toList();
    }

    public synchronized PacienteResposta buscar(String id) {
        return PacienteResposta.de(obter(id));
    }

    public synchronized PacienteResposta atualizar(String id, AtualizacaoPaciente entrada) {
        Paciente paciente = obter(id);
        if (entrada == null) {
            throw new IllegalArgumentException("Os dados de atualização são obrigatórios.");
        }
        paciente.atualizar(entrada.nome(), paciente.getDataNascimento(), entrada.telefone(),
                entrada.email(), entrada.endereco());
        return PacienteResposta.de(paciente);
    }

    /** Uso interno pelos módulos que precisam associar um paciente cadastrado. */
    public synchronized Paciente obter(String id) {
        Paciente paciente = registros.get(id);
        if (paciente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado.");
        }
        return paciente;
    }
}
