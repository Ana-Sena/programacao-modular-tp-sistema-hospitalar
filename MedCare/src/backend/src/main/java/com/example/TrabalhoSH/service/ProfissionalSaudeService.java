package com.example.TrabalhoSH.service;

import com.example.TrabalhoSH.model.*;
import com.example.TrabalhoSH.controller.ProfissionalSaudeRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Cadastro em memória compartilhado pelos módulos de consultas e internações. */
@Service
public class ProfissionalSaudeService {
    private final Map<String, ProfissionalSaude> profissionais = new LinkedHashMap<>();

    public synchronized List<ProfissionalSaude> listar() {
        return List.copyOf(profissionais.values());
    }

    public synchronized ProfissionalSaude cadastrar(ProfissionalSaudeRequest request) {
        ProfissionalSaude profissional = criarProfissional(request);
        profissionais.put(profissional.getId(), profissional);
        return profissional;
    }

    public synchronized ProfissionalSaude obter(String id) {
        ProfissionalSaude profissional = profissionais.get(id);
        if (profissional == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional de saúde não encontrado.");
        }
        return profissional;
    }

    private ProfissionalSaude criarProfissional(ProfissionalSaudeRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O corpo da requisição é obrigatório.");
        }

        if (request.getTipo() == null || request.getTipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo 'tipo' é obrigatório.");
        }

        String tipo = request.getTipo().trim().toLowerCase(Locale.ROOT).replaceAll("[\\s_-]", "");
        ProfissionalSaude profissional = switch (tipo) {
            case "clinicogeral", "clínicogeral" -> new ClinicoGeral();
            case "especialista" -> new Especialista();
            case "cirurgiao", "cirurgião" -> new Cirurgiao();
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tipo de profissional inválido. Use 'clinicoGeral', 'especialista' ou 'cirurgiao'."
            );
        };

        profissional.setNome(request.getNome());
        profissional.setRegistroProfissional(request.getRegistroProfissional());
        profissional.setEspecialidade(request.getEspecialidade());
        profissional.setTelefone(request.getTelefone());
        profissional.setEmail(request.getEmail());

        return profissional;
    }
}
